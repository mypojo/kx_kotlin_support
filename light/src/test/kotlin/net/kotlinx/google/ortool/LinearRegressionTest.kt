package net.kotlinx.google.ortool

import net.kotlinx.core.number.halfUp
import net.kotlinx.core.string.toTextGrid
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.math.LinearRegression
import net.kotlinx.math.PolynomialRegression
import org.junit.jupiter.api.Test

class LinearRegressionTest : BeSpecLog(){
    init {
        @Test
        fun test() {

            val inputDatas = listOf(1L, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            val outputDatas = listOf(25L, 35, 49, 60, 75, 90, 115, 130, 150, 200)
            val reg = LinearRegression(inputDatas, outputDatas)

            val predicData = listOf(2.5, 12.0)
            val newInputDatas = inputDatas.map { it.toDouble() } + predicData

            listOf("입력", "결과").toTextGrid(newInputDatas.sorted().map { arrayOf(it, reg[it].halfUp(2)) }).print()

            //다항 회긔 옵션

            val x = inputDatas.map { it.toDouble() }
            val y = outputDatas.map { it.toDouble() }
            val regression = PolynomialRegression(x.toDoubleArray(), y.toDoubleArray(), 1) //1차
            val newInputDatas2 = x.map { it } + predicData

            listOf("입력", "결과").toTextGrid(newInputDatas2.sorted().map { arrayOf(it, regression.predict(it).halfUp(2)) }).print()

        }
    }
}