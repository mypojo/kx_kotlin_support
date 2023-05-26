package net.kotlinx.core.concurrent

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * 간단한 코루틴 실행기.
 * 리턴받을게 있다면 async & await
 * Semaphore로 인한 약간의 지연은 무시한다
 *
 * 경고!! CoroutineScope를 따로 전달받지 않고, 새로 열어준다
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