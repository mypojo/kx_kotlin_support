package net.kotlinx.core.string

import io.kotest.matchers.shouldBe
import net.kotlinx.core.time.toKr01
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

internal class StringTimeSupportKtTest : BeSpecLog() {
    init {
        initTest(KotestUtil.FAST)

        Given("StringTimeSupport.kt") {
            When("toLocalDateTime") {
                Then("string -> LocalDateTime 변환") {
                    "2022-12-31".toLocalDateTime().toKr01() shouldBe "2022년12월31일(토) 00시00분00초"
                    "20221231-11:30:12".toLocalDateTime().toKr01() shouldBe "2022년12월31일(토) 11시30분12초"
                    "2023-01-04T14:09:12.952065".toLocalDateTime().toKr01() shouldBe "2023년01월04일(수) 14시09분12초"
                }
            }
        }
    }
}