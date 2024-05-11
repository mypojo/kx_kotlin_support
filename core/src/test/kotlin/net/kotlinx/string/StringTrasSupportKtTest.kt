package net.kotlinx.string

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class StringTrasSupportKtTest : BehaviorSpec() {
    init {
        initTest(KotestUtil.FAST)

        Given("StringTrasSupportKt") {
            Then("간단체크") {
                "12345".isNumeric() shouldBe true
                "123.45".isNumeric() shouldBe false
            }
        }
    }
}
