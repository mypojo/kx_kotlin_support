package net.kotlinx.core.string

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import net.kotlinx.core.regex.RegexSet
import net.kotlinx.core.test.KotestUtil

internal class StringRegexSupportKt2Test : DescribeSpec({

    tags(KotestUtil.FAST, KotestUtil.TESTING)

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
    }.also {
        listOf("문자열", "regex", "retainFrom", "removeFrom").toTextGrid(it).print()
    }

    describe("StringRegexSupport.kt") {
        val text1 = "2022-12-24.7KBV"
        context("retainFrom") {
            println("xxxxxxxxxxxx vvvvvvvvvvvvvv")
            it("해당 매칭만 남기고 제거") {
                text1.retainFrom(RegexSet.NUMERIC) shouldBe "202212247"
                123 shouldBe "1234"
            }
        }
        context("removeFrom") {
            it("해당 매칭을 남기고 제거") {
                text1.removeFrom(RegexSet.NUMERIC) shouldBe "--.KBV"
            }
            println("***************************")
        }
    }

})