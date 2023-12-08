package net.kotlinx.core.concurrent

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.time.Duration

/**
 * 간단한 작업 실행용.
 * readCsvLines 한 데이터를 vo로 변환시 이걸 사용하면 30초 -> 10초 정도로 줄어들 수 있음
 * @see Runtime.getRuntime().availableProcessors()
 *  */
fun <T> List<Callable<T>>.parallelExecute(threadCnt: Int = this.size): List<T> {
    val ex = Executors.newFixedThreadPool(threadCnt)!!
    val results = this.map { ex.submit(it) }.map { it.get() } //null일 수 있음 (리턴 없는것들)
    ex.shutdown()
    return results
}

/** 간단 sleep */
fun Duration.sleep(){
    Thread.sleep(this.inWholeMilliseconds)
}