package net.kotlinx.concurrent

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.time.Duration


/**
 * 간단한 코루틴 실행기 (스코프 있는경우)
 * 근데 굳이 이렇게 할 필요없이 새로 열어도 잘 작동함
 * 경고!! 이거 대신 flow & merge 를 사용하세요 -> Flow<T>.execute
 * */
@Deprecated("use flow")
suspend fun <T> List<suspend () -> T>.coroutineExecute(scope: CoroutineScope, maxConcurrency: Int = Int.MAX_VALUE): List<T> {
    val gate = Semaphore(maxConcurrency)
    return this.map {
        scope.async {
            gate.withPermit { it() }
        }
    }.map {
        it.await()
    }
}

/**
 * 간단한 코루틴 실행기. (CoroutineScope를 새로실행)
 * 리턴받을게 있다면 async & await
 * 참고로 리턴받을게 없다면  launch 써도됨
 * Semaphore로 인한 약간의 지연은 무시한다
 * ex) OkHttp 의 await 사용.  참고로 fetch 로 하면 적용안됨 주의!!
 * 경고!! 이거 대신 flow & merge 를 사용하세요 -> Flow<T>.execute
 * @see coroutine  <-- 이거로 쓸것!
 * */
fun <T> List<suspend () -> T>.coroutineExecute(maxConcurrency: Int = Int.MAX_VALUE): List<T> {
    val gate = Semaphore(maxConcurrency)
    val list = this
    return runBlocking {
        return@runBlocking list.map {
            async {
                gate.withPermit { it() }
            }
        }.map {
            it.await()
        }
    }
}

/**
 * 간단한 코루틴 실행기.
 * 리턴받을게 없다면 launch & join
 * @see coroutine  <-- 이거로 쓸것!
 *  */
fun List<suspend () -> Unit>.coroutineExecute(maxConcurrency: Int = Int.MAX_VALUE) {
    val gate = Semaphore(maxConcurrency)
    val list = this
    runBlocking {
        list.map {
            launch {
                gate.withPermit { it() }
            }
        }.map {
            it.join()
        }
    }
}


/**
 * 간단한 코루틴 실행기
 * coroutineScope 를 사용해서, 예외 전파를 제한해준다
 * 인라인 처리도 되지만 semaphore 때문에 별도로 뺐다
 * */
suspend fun <IN, OUT> List<IN>.coroutine(maxConcurrency: Int = Int.MAX_VALUE, block: suspend (IN) -> OUT): List<OUT> {
    val semaphore = Semaphore(maxConcurrency)
    val list = this
    return coroutineScope {
        list.map { input -> async { semaphore.withPermit { block(input) } } }.awaitAll()
    }
}


/** 간단 delay. */
suspend fun Duration.delay() {
    delay(this)
}