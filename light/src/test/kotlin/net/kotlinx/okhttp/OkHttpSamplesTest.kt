package net.kotlinx.okhttp

import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.time.TimeStart
import net.kotlinx.time.measureTimeString

class OkHttpSamplesTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("OkHttpSamples") {
            Then("달러-원 가져오기 (동기화)") {
                printName()
                val timeString = measureTimeString {
                    (0..3).map {
                        suspend {
                            val start = TimeStart()
                            log.info { "작업시작 $it .." }
                            val won = OkHttpSamples.dollarWonFetch()
                            log.info { "작업종료 $it -> $won  $start" }
                        }
                    }.coroutineExecute()
                }
                log.warn { "=== 동기호출 $timeString" }
            }

            Then("달러-원 가져오기 (비동기화)") {
                printName()
                val timeString = measureTimeString {
                    (0..3).map {
                        suspend {
                            val start = TimeStart()
                            log.info { "작업시작 $it .." }
                            val won = OkHttpSamples.dollarWonAwait()
                            log.info { "작업종료 $it -> $won  $start" }
                        }
                    }.coroutineExecute()
                }
                log.warn { "=== 동기호출 $timeString" }
            }
        }
    }

}