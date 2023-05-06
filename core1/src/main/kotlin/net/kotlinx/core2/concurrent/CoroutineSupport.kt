package net.kotlinx.core2.concurrent

import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * 간단한 코루틴 실행기.
 * 리턴받을게 있다면 async & await
 * Semaphore로 인한 약간의 지연은 무시한다
 * */
fun <T> List<suspend () -> T?>.coroutineExecute(maxConcurrency: Int = Int.MAX_VALUE): List<T?> {
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