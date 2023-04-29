package net.kotlinx.core2.concurrent

import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/** 간단한 코루틴 실행기 */
fun <T> List<suspend () -> T?>.coroutineExecute(): List<T?> {
    val list = this
    return runBlocking {
        return@runBlocking list.map {
            async { it() }
        }.map {
            it.await()
        }
    }
}

/** 간단한 코루틴 실행기. */
fun List<suspend () -> Unit>.coroutineExecute() {
    val list = this
    runBlocking {
        list.map {
            launch { it() }
        }.map {
            it.join()
        }
    }
}