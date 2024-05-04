package net.kotlinx.core.number

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

internal class NumberShortenersTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("Base62Util") {
            Then("변환 / 역변환") {
                val value1 = 1231298123791823L
                val text = NumberShorteners.toBase62(value1)
                val value2 = NumberShorteners.fromBase62(text)
                value1 shouldBe value2
                log.info { "변환 $value1 -> $text" }
            }
        }
    }

}