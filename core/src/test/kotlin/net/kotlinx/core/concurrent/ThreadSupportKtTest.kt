package net.kotlinx.core.concurrent

import io.kotest.matchers.longs.shouldBeInRange
import net.kotlinx.core.time.measureTimeString
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import java.util.concurrent.Callable
import kotlin.time.Duration.Companion.seconds

class ThreadSupportKtTest : BeSpecLog() {

    init {
        initTest(KotestUtil.SLOW)

        Given("parallelExecute") {
            val duration = 2.seconds
            Then("$duration 이내로 끝나야함") {
                val timeString = measureTimeString {
                    (0..6).map {
                        Callable {
                            log.debug { " -> 작업시작.." }
                            Thread.sleep(duration.inWholeMilliseconds)
                            it
                        }
                    }.parallelExecute(10)
                }
                timeString.millis shouldBeInRange (duration.inWholeMilliseconds..duration.inWholeMilliseconds + 100)
            }
        }
    }


}