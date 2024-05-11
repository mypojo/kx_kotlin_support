package net.kotlinx.calculator

import net.kotlinx.concurrent.sleep
import net.kotlinx.counter.EventTimeChecker
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ProgressInlineCheckerTest : BeSpecLog() {
    init {

        initTest(KotestUtil.SLOW)

        Given("ProgressInlineChecker") {
            Then("진행율 체크됨") {
                val total: Long = 40
                val counter = ProgressInlineChecker(total, EventTimeChecker(1.seconds))

                for (i in 0 until total) {
                    counter.check()
                    150.milliseconds.sleep()
                }
            }
        }
    }
}