package net.kotlinx.core2.calculator

import mu.KotlinLogging
import net.kotlinx.core1.counter.EventTimeChecker
import net.kotlinx.core2.test.TestLevel02
import net.kotlinx.core2.test.TestRoot
import kotlin.time.Duration.Companion.seconds

class ProgressInlineCheckerTest : TestRoot() {

    private val log = KotlinLogging.logger {}

    @TestLevel02
    fun test() {
        val total: Long = 50
        val counter = ProgressInlineChecker(total, EventTimeChecker(1.seconds))

        for (i in 0 until total) {
            counter.check()
            Thread.sleep(1.seconds.inWholeMilliseconds)
        }
    }

}