package net.kotlinx.core.collection

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class RangeMapTest : BeSpecLog() {
    init {
        initTest(KotestUtil.FAST)

        Given("RangeMap") {

            val rangeMap = RangeMap(
                listOf(
                    "20230701".."20230706" to "data2",
                    "20230101".."20231201" to "data1",
                )
            )

            Then("기본사용법 테스트") {
                rangeMap["20230301"] shouldBe "data1"
                rangeMap["20230702"] shouldBe "data2"
                rangeMap["20220101"] shouldBe null

                rangeMap["20230101"] shouldNotBe null
                rangeMap["20231201"] shouldNotBe null
            }
        }
    }
}