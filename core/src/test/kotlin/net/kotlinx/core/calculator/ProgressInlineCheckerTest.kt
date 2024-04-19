package net.kotlinx.core.calculator

import net.kotlinx.core.concurrent.sleep
import net.kotlinx.core.counter.EventTimeChecker
import net.kotlinx.test.TestLevel02
import net.kotlinx.test.TestRoot
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ProgressInlineCheckerTest : TestRoot() {

    @TestLevel02
    fun `프로그레스 체크`() {
        val total: Long = 50
        val counter = ProgressInlineChecker(total, EventTimeChecker(1.seconds))

        for (i in 0 until total) {
            counter.check()
            200.milliseconds.sleep()
        }
    }

}