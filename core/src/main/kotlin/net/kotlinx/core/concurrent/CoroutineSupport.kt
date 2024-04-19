package net.kotlinx.core.concurrent

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.time.Duration


/**
 * 간단한 코루틴 실행기 (스코프 있는경우)
 * 근데 굳이 이렇게 할 필요없이 새로 열어도 잘 작동함
 * */
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
 * Semaphore로 인한 약간의 지연은 무시한다
 * ex) OkHttp 의 await 사용.  참고로 fetch 로 하면 적용안됨 주의!!
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
 * Flow를 한번에 다 실행해서  결과를 반환한다. 주의해서 사용!!
 * ex) paging 설정에서 모든 페이지 로드
 * */
suspend fun <T, R> Flow<T>.collectToList(block: (T) -> R): List<R> {
    val list = mutableListOf<R>()
    this.collect {
        val r = block(it)
        list += r
    }
    return list
}

/** 간단 delay. */
suspend fun Duration.delay() {
    delay(this)
}