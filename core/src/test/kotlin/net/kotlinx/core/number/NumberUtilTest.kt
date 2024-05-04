package net.kotlinx.core.number

import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class NumberUtilTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("NumberUtil") {
            Then("패딩 사이즈를 구함") {
                NumberUtil.numPadSize(1000) shouldBe 3
                NumberUtil.numPadSize(1001) shouldBe 4
                NumberUtil.numPadSize(99) shouldBe 2
            }
        }
    }

}