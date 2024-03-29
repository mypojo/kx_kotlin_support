package net.kotlinx.core.concurrent

import kotlinx.coroutines.delay
import net.kotlinx.core.time.measureTimeString
import net.kotlinx.test.TestLevel02
import net.kotlinx.test.TestRoot
import java.util.concurrent.Callable
import kotlin.time.Duration.Companion.seconds

class CoroutineSupportKtTest : TestRoot() {

    /** 코루틴 & 스래드 테스트 */
    @TestLevel02
    fun test() {

        val datas = listOf(2, 4, 1, 5, 2).map {
            suspend {
                log.debug { " -> [$it] 실행대기..." }
                delay(it.seconds.inWholeMilliseconds)
                log.debug { " -> [$it] 실행종료" }
                "$it 초 딜레이"
            }
        }

        mapOf(
            10 to 5.seconds,
            3 to 6.seconds,
            2 to 8.seconds,
        ).entries.map { (maxConcurrency, timeout) ->
            Callable {
                measureTimeString {
                    //내부는 코루틴으로 작동
                    datas.coroutineExecute(maxConcurrency).mapIndexed { index, it -> "결과 : list[$index] = $it" }.also { log.info { "결과 : $it" } }
                }.also {
                    log.info { " => [${Thread.currentThread().name}] 걸린시간 : $it " }
                    check(it.millis <= timeout.inWholeMilliseconds + 200) { " 타임아웃 초과!!" } //버퍼 추가
                }
            }
        }.parallelExecute() //각 항목들은 스래드로 측정
    }



}