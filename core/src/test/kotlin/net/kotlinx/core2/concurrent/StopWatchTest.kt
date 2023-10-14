package net.kotlinx.core2.concurrent

import net.kotlinx.core.concurrent.parallelExecute
import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test
import java.util.concurrent.Callable
import kotlin.random.Random

class StopWatchTest : TestRoot() {

    val stopWatch = StopWatch()

    @Test
    fun 체크샘플() {

        (0..15).map {
            Callable {
                for (i in 0..16) {
                    stopWatch.check("STEP01") {
                        Thread.sleep(15)
                    }
                    stopWatch.check("STEP02") {
                        //랜덤하게 작업 스킵
                        if (Random.nextInt(10) % 10 != 0) {
                            Thread.sleep(84)
                        }
                    }
                    stopWatch.check("STEP03") {
                        Thread.sleep(22)
                    }
                }
            }
        }.parallelExecute(10)

        println(stopWatch)
    }

}