package net.kotlinx.core.test.mockk

import io.mockk.spyk
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

/**
 *  Mockk 테스트 샘플
 */
class MockkSpykTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        class Calculator {

            fun plus(a: Int, b: Int): Int {
                return privatePlus(a, b)
            }

            private fun privatePlus(a: Int, b: Int): Int {
                return a - b
            }
        }

        Given("MockkSpyk") {
            Then("1") {

                val calculator = spyk<Calculator>()
                spyk(Calculator()) //이렇게도 됨

                calculator.plus(1, 2) //스터빙 안해도 오류 안남
            }
        }
    }

}