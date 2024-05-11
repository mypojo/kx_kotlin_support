package net.kotlinx.concurrent

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import java.util.concurrent.Callable
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class StopWatchTest : BeSpecLog() {
    init {
        initTest(KotestUtil.SLOW)


        Given("StopWatch") {
            Then("병렬처리 환경에서 각 구간의 실행시간을 기록/누적해서 표현") {
                val stopWatch = StopWatch()
                (0..15).map {
                    Callable {
                        for (i in 0..16) {
                            stopWatch.check("STEP01") {
                                Random.nextInt(20).milliseconds.sleep()
                            }
                            stopWatch.check("STEP02") {
                                Random.nextInt(80).milliseconds.sleep()
                            }
                            stopWatch.check("STEP03") {
                                Random.nextInt(30).milliseconds.sleep()
                            }
                        }
                    }
                }.parallelExecute(10)

                println(stopWatch)
            }
        }
    }
}