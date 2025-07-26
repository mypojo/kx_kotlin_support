package net.kotlinx.string

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.regex.RegexSet
import net.kotlinx.regex.removeFrom
import net.kotlinx.regex.retainFrom

internal class StringRegexSupportKtTest : BeSpecLog() {
    init {
        initTest(KotestUtil.FAST)



        Given("StringRegexSupport.kt") {

            Then("변환 샘플") {
                listOf("문자열", "정규식", "retainFrom", "removeFrom").toTextGridPrint {
                    listOf(
                        "1234" to RegexSet.NUMERIC,
                        "1234.56" to RegexSet.NUMERIC,
                        "1234.567" to RegexSet.NUMERIC_DOT,
                        "1234" to RegexSet.NUMERIC,
                    ).map { (text, regex) ->
                        arrayOf(
                            text, regex.pattern,
                            text.retainFrom(regex),
                            text.removeFrom(regex),
                        )
                    }
                }
            }

            val demo = "2022-12-24.7KBV"

            When("retainFrom") {
                Then("해당 매칭만 남기고 제거") {
                    demo.retainFrom(RegexSet.NUMERIC) shouldBe "202212247"
                }
            }
            When("removeFrom") {
                Then("해당 매칭을 남기고 제거") {
                    demo.removeFrom(RegexSet.NUMERIC) shouldBe "--.KBV"
                }
            }
        }
    }
}