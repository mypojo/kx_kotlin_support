package net.kotlinx.core2.concurrent

import kotlinx.coroutines.delay
import net.kotlinx.core2.test.TestRoot
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class CoroutineSupportKtTest : TestRoot() {


    @Test
    fun test() {

        val list = listOf(2, 4, 1, 5,2).map {
            suspend {
                log.info { "[$it] 실행대기..." }
                delay(it.seconds.inWholeMilliseconds)
                log.info { "[$it] 실행종료" }
                "$it"
            }
        }

        list.coroutineExecute(2).forEachIndexed { index, it ->
            log.info { "결과[$index] : $it" }
        }

    }

}