package net.kotlinx.concurrent

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import java.util.concurrent.Callable
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

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

            Then("간단 샘플은 이거"){
                val clock = TimeSource.Monotonic
                val startTime = clock.markNow()

                // 구간 A
                // 코드 A 실행
                val timeA = clock.markNow()
                println("구간 A 소요 시간: ${timeA - startTime}")

                // 구간 B
                // 코드 B 실행
                val timeB = clock.markNow()
                println("구간 B 소요 시간: ${timeB - timeA}")

                // 구간 C
                // 코드 C 실행
                val timeC = clock.markNow()
                println("구간 C 소요 시간: ${timeC - timeB}")

                println("전체 소요 시간: ${timeC - startTime}")
            }


        }
    }
}