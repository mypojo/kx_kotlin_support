package net.kotlinx.core.time

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.contain
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

internal class TimeStringTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("TimeString") {
            Then("시간 -> 한글변환 체크") {
                val duration = 3.hours + 20.minutes
                duration.inWholeMilliseconds.toTimeString().toString() shouldBe contain("3시간")
            }
        }
    }


}