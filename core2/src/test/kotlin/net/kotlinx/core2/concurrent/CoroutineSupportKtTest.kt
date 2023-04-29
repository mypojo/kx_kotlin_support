package net.kotlinx.core2.concurrent

import kotlinx.coroutines.delay
import net.kotlinx.core1.time.TimeStart
import net.kotlinx.core2.test.TestRoot
import org.junit.jupiter.api.Test

class CoroutineSupportKtTest : TestRoot() {


    @Test
    fun test() {

        val list = listOf(
            suspend {
                log.info { "실행대기..." }
                delay(2000)
                log.info { "실행종료" }
                "aaa"
            },
            suspend {
                log.info { "실행대기..." }
                delay(3000)
                log.info { "실행종료" }
                "bbb"
            },
            suspend {
                log.info { "실행대기..." }
                delay(4000)
                log.info { "실행종료" }
                "ccc"
            },
        )

        val start = TimeStart()
        list.coroutineExecute().forEachIndexed { index, it ->
            log.info { "결과[$index] : $it" }
        }

    }

}