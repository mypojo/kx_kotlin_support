package net.kotlinx.core2.concurrent

import mu.KotlinLogging
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 큐를 사용한 마이크로 배치 처리기 without 코루틴
 */
class MicroBatchQueue(
    capacity: Int = 10000,
    interval: Duration = 1.seconds,
    private val microBatch: (List<String>) -> Unit,
) {

    private val log = KotlinLogging.logger {}

    private val sleeper: ThreadSleepTool = ThreadSleepTool(interval)
    private val queue: BlockingQueue<String> = ArrayBlockingQueue(capacity)

    private val theThread: Thread = Thread {
        val current = Thread.currentThread()
        log.info { "[${current.name}] thread start.." }
        try {
            while (!current.isInterrupted) {
                sleeper.checkAndSleep()
                var data = queue.poll()
                if (data != null) {
                    val list = mutableListOf<String>()
                    while (data != null) {
                        list += data
                        data = queue.poll()
                    }
                    microBatch.invoke(list)
                }
            }
        } catch (e: InterruptedException) {
            //아무것도 하지않음
        }
        log.info { "[${current.name}] thread end" }
    }.also {
        it.isDaemon = true
    }

    /** 큐 한도 넘으면 에러남 */
    fun add(value: String) = queue.add(value)

    /** 싱글 스래드로 작동한다. */
    fun start() = theThread.start()

    /** 싱글 스래드로 작동한다. */
    fun interrupt() = theThread.interrupt()

}