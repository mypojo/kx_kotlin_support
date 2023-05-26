package net.kotlinx.core.concurrent

import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * 간단한 작업 실행용. 해보니 Callable 을 단축 하는건 크게 의미 없음..
 * @see Runtime.getRuntime().availableProcessors()
 *  */
fun <T> List<Callable<T>>.parallelExecute(threadCnt: Int = this.size): List<T> {
    val ex = Executors.newFixedThreadPool(threadCnt)!!
    val results = this.map { ex.submit(it) }.map { it.get() } //null일 수 있음 (리턴 없는것들)
    ex.shutdown()
    return results
}