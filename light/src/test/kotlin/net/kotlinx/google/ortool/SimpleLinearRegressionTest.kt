package net.kotlinx.google.ortool

import net.kotlinx.core.number.halfUp
import net.kotlinx.core.string.toTextGrid
import net.kotlinx.math.SimpleLinearRegression
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class SimpleLinearRegressionTest : TestRoot() {


    @Test
    fun test() {

        val inputDatas = listOf(1L, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val reg = SimpleLinearRegression(
            inputDatas,
            listOf(25, 35, 49, 60, 75, 90, 115, 130, 150, 200)
        )

        val newInputDatas = inputDatas.map { it.toDouble() } + listOf(2.5, 12.0)

        listOf("입력", "결과").toTextGrid(newInputDatas.sorted().map { arrayOf(it, reg[it].halfUp(2)) }).print()

    }

}