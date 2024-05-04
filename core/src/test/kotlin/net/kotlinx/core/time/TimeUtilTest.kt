package net.kotlinx.core.time

import io.kotest.matchers.shouldBe
import net.kotlinx.core.string.toLocalDate
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class TimeUtilTest : BeSpecLog() {
    init {
        initTest(KotestUtil.FAST)

        Given("TimeUtil") {
            Then("between") {

                val start = "20231201".toLocalDate()
                val end = "20231205".toLocalDate()
                val dates = TimeUtil.between(start, end)
                dates.size shouldBe 5
            }
        }
    }
}