package net.kotlinx.core1.string

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import net.kotlinx.core1.regex.RegexSet

internal class StringRegexSupportKtTest : DescribeSpec({

    describe("StringRegexSupport.kt") {
        val text1 = "2022-12-24.7KBV"
        context("retainFrom") {
            it("해당 매칭만 남기고 제거") {
                text1.retainFrom(RegexSet.NUMERIC) shouldBe "202212247"
            }
        }
        context("removeFrom") {
            it("해당 매칭을 남기고 제거") {
                text1.removeFrom(RegexSet.NUMERIC) shouldBe "--.KBV"
            }
        }
    }

})