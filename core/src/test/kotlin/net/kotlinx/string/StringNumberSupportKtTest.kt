package net.kotlinx.string

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class StringNumberSupportKtTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("StringNumberSupportKt") {
            Then("toLong2") {
                "10.2".toLong2() shouldBe 10
                "10.5".toLong2() shouldBe 11
                "12.9".toLong2() shouldBe 13
            }
        }
    }
}