package net.kotlinx.core.test.mockk

import io.mockk.spyk
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

/**
 *  Mockk 테스트 샘플
 */
class MockkSpykTest : TestRoot() {

    class Calculator {

        fun plus(a: Int, b: Int): Int {
            return privatePlus(a, b)
        }

        private fun privatePlus(a: Int, b: Int): Int {
            return a - b
        }
    }

    @Test
    fun test() {
        val calculator = spyk<Calculator>()
        spyk(Calculator()) //이렇게도 됨

        calculator.plus(1, 2) //스터빙 안해도 오류 안남
    }
}