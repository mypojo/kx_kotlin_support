package net.kotlinx.string

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import java.math.BigDecimal

class StringNumberSupportKtTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("StringNumberSupportKt") {
            Then("toLong2") {
                "10.2".toLong2() shouldBe 10
                "10.5".toLong2() shouldBe 11
                "12.9".toLong2() shouldBe 13
            }

            Then("toBigDecimal2") {
                "입력데이터 986.87".toBigDecimal2() shouldBe BigDecimal("986.87")
                "1.23E+8".toBigDecimal2() shouldBe BigDecimal("123000000.00")
                "9.99E-5".toBigDecimal2() shouldBe BigDecimal("0.0000999")
            }
        }
    }
}