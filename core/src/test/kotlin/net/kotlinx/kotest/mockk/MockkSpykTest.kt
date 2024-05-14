package net.kotlinx.kotest.mockk

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKException
import io.mockk.every
import io.mockk.mockk
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

            /** 모킹 안함 */
            fun plus(a: Int, b: Int): Int = privatePlus(a, b)

            private fun privatePlus(a: Int, b: Int): Int = a + b + 1

            fun minus(a: Int, b: Int): Int = a - b

        }

        Given("spyk 는 스터핑 하지 않은것들은 모킹하지 않는다") {

            When("특정 메소드만 모킹하고싶음") {

                Then("mockk로 생성후 plus를 호출하면 에러남 (privatePlus가 없음)") {
                    shouldThrow<MockKException> {
                        val calculator = mockk<Calculator> {
                            // private fun 도 모킹이 가능하긴 하지만 옵션과 코딩이 귀찮다
                        }
                        calculator.plus(1, 2)
                    }
                }

                Then("spyk 로 생성하면 정상 작동") {
                    val calculator = spyk<Calculator> {
                        every { minus(any(), any()) } returns 999
                    }
                    //spyk(Calculator()) //이렇게도 됨
                    calculator.plus(1, 2) shouldBe 4
                    calculator.minus(1, 2) shouldBe 999
                }
            }
        }


    }
}