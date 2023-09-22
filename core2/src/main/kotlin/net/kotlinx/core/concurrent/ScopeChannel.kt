package net.kotlinx.core.concurrent

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.sync.Semaphore
import mu.KotlinLogging
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/** 가능한 모든 요소를 가져온다. (suspend xx) 어차피 인메모리라 limit 없음. */
fun <E> ReceiveChannel<E>.tryReceiveAvailable(): List<E> {
    if (isEmpty) return emptyList()

    val msgs = mutableListOf<E>()
    while (true) { //true 로 해도 된다.
        val next = tryReceive()
        if (next.isFailure) break
        msgs += next.getOrThrow()
    }
    return msgs
}

/** 가능한 1개 이상의 모든 요소를 가져온다. (suspend ㅇㅇ) 어차피 인메모리라 limit 없음. */
suspend fun <E> ReceiveChannel<E>.receiveAvailable(): List<E> {
    val msgs = mutableListOf<E>()
    msgs += receive() //첫 객체는 기다린다.
    while (true) {
        val next = tryReceive()
        if (next.isFailure) break
        msgs += next.getOrThrow()
    }
    return msgs
}

/**
 * 소스코드 참고용 채널 (실무사용 xx)
 * ex) 크롤링 서버 (동시 실행제한 & 횟수 제한으로 IP블록 회피)
 * Channel 위임 안함
 * 예외 발생시 채널의 데이터는 다 소실된다.
 *  */
class ScopeChannel(
    /** 기본 스코프. 예외 발생시 이 단위로 다 중지 */
    private val scope: CoroutineScope,
    /** 채널 capacity */
    capacity: Int = Channel.UNLIMITED,
    /** 기본 딜레이 */
    delay: Long = 0,
    /** 최대 동시 실행 수 제한 */
    private val maxConcurrency: Int = 1000,
    /** 처리당 기본 타임아웃 */
    private val timeout: Long = TimeUnit.SECONDS.toMillis(20),
    /** 타임아웃 콜백. throw 해봐야 반응 안함(전체를 취소시키지 않음). 알람 or 큐처리 할것 */
    private val timeoutCallback: suspend (TimeoutCancellationException, data: Any) -> Unit = DEFAULT_TIMEOUT_CALLBACK,
    /**
     * 개별 로직의 예외 처리 콜백
     * JobCancellationException
     * TimeoutCancellationException : 던져져도 반응 안함.
     *  */
    private val exCallback: suspend (Throwable, data: Any) -> Unit = { e, _ -> throw e },
) {

    private val log = KotlinLogging.logger {}

    val context = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

    /** 위임 채널 */
    private val channel: Channel<String> = Channel(capacity)

    /** 대기도구 */
    private val sleeper: CoroutineSleepTool = CoroutineSleepTool(delay)

    /** 동시성 실행 제한기 */
    private val semaphore = Semaphore(maxConcurrency)

    /** 개략적인 모니터링을 위한 카운터 */
    private val _queueCnt = AtomicLong()
    val cnt: Long
        get() = _queueCnt.get()

    /** 닫기 */
    fun close(cause: Throwable? = null): Boolean = channel.close(cause)

    suspend fun startMonitoring(monitoringDelay: Long = TimeUnit.SECONDS.toMillis(5)) {
        val monitoringSleeper = CoroutineSleepTool(monitoringDelay)
        scope.launch(context) {
            log.debug { "모니터링 시작.." }
            while (!channel.isClosedForReceive) { //모두 처리될때까지 수행함
                monitoringSleeper.checkAndSleep()
                log.info { " -> 처리상태 : 실행중 ${maxConcurrency - semaphore.availablePermits} / 전체큐 $cnt" }
            }
            log.debug { "모니터링 종료.." }
        }
    }

    /** 단건 처리 */
    suspend fun receive(doCallback: suspend (String) -> Unit) {
        silenceClose {
            while (true) {
                semaphore.acquire()
                sleeper.checkAndSleep()
                val data = channel.receive() //명시적으로 가져온 후 예외를 잡는 방법 선택
                executeLaunch(doCallback, data)
            }
        }
        log.warn { "channel closed -> receive 종료" }
    }

    /** 배치(모아서) 처리 */
    suspend fun receiveBatch(doCallback: suspend (List<String>) -> Unit) {
        silenceClose {
            while (!channel.isClosedForReceive) {
                semaphore.acquire()
                sleeper.checkAndSleep()
                val datas = channel.receiveAvailable() //명시적으로 가져온 후 예외를 잡는 방법 선택
                if (datas.isNotEmpty()) {
                    executeLaunch(doCallback, datas)
                }
            }
        }
        log.warn { "channel closed -> receive 종료" }
    }

    private suspend fun <T> executeLaunch(doCallback: suspend (T) -> Unit, data: T) {
        scope.launch(context) {
            try {
                withTimeout(timeout) {
                    try {
                        doCallback.invoke(data)
                    } finally {
                        semaphore.release()
                        _queueCnt.decrementAndGet()
                    }
                }
            } catch (e: TimeoutCancellationException) {
                timeoutCallback.invoke(e, data as Any)
            } catch (e: Throwable) {
                exCallback.invoke(e, data as Any)
            }
        }
    }

    suspend fun send(element: String) {
        channel.send(element)
        _queueCnt.incrementAndGet()
    }

    companion object {

        private val log = KotlinLogging.logger {}

        val DEFAULT_TIMEOUT_CALLBACK: suspend (TimeoutCancellationException, data: Any) -> Unit = { e, data ->
            log.debug { "$e / input = $data" }
        }

        /** 예외를 잡아서 아무것도 하지 않는다. */
        suspend fun silenceClose(run: suspend () -> Unit) {
            try {
                run.invoke()
            } catch (e: ClosedReceiveChannelException) {
                //아무것도 하지 않음
            }
        }
    }
}
