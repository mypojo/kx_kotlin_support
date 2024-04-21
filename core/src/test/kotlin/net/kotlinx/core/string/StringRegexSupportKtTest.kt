package net.kotlinx.core.string

import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import mu.KotlinLogging
import net.kotlinx.core.regex.RegexSet

private val log = KotlinLogging.logger {}

@Tags("L2")
internal class StringRegexSupportKtTest : DescribeSpec({

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

    beforeEach {
        println("Hello from ${it.descriptor.id.value}")
    }

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
            println("xxxxxx")
        }
    }

})