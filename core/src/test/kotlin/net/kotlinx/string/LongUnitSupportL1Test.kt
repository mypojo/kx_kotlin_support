package net.kotlinx.string

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.number.toSiText

internal class LongUnitSupportL1Test : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("LongUnitSupport") {
            Then("toSiText") {
                19915640000.toSiText() shouldBe "18 gbyte"
            }
        }
    }
}