package net.kotlinx.core.calculator

import io.kotest.core.spec.style.BehaviorSpec
import net.kotlinx.core.concurrent.sleep
import net.kotlinx.core.counter.EventTimeChecker
import net.kotlinx.core.test.KotestUtil
import net.kotlinx.core.test.init
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ProgressInlineCheckerTest : BehaviorSpec({

    init(KotestUtil.SLOW)

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
})