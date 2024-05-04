package net.kotlinx.core.regex

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class RegexSetTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("RegexSet") {
            Then("extract") {
                val text = "<tag1>ab영감님</tag1>"
                val pattern = RegexSet.extract("<tag1>", "</tag1>").toRegex()
                pattern.find(text)!!.value shouldBe "ab영감님"
            }
        }
    }


}