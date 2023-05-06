package net.kotlinx.core2.concurrent

import kotlinx.coroutines.delay
import net.kotlinx.core1.time.measureTime
import net.kotlinx.core2.test.TestLevel02
import net.kotlinx.core2.test.TestRoot
import kotlin.time.Duration.Companion.seconds

class CoroutineSupportKtTest : TestRoot() {


    @TestLevel02
    fun test() {

        val list = listOf(2, 4, 1, 5, 2).map {
            suspend {
                log.debug { " -> [$it] 실행대기..." }
                delay(it.seconds.inWholeMilliseconds)
                log.debug { " -> [$it] 실행종료" }
                "$it 초 딜레이"
            }
        }

        measureTime {
            val maxConcurrency = 2
            list.coroutineExecute(maxConcurrency).mapIndexed { index, it -> "결과 : list[$index] = $it" }.also { log.info { "결과 : $it" } }
        }.also {
            log.info { " => 걸린시간 : $it " }
            check(it.millis <= 8.5.seconds.inWholeMilliseconds)
        }

        measureTime {
            list.coroutineExecute().mapIndexed { index, it -> "결과 : list[$index] = $it" }.also { log.info { "결과 : $it" } }
        }.also {
            log.info { " => 걸린시간 : $it " }
            check(it.millis <= 5.5.seconds.inWholeMilliseconds)
        }

    }

}