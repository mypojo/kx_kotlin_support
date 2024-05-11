package net.kotlinx.math

import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.number.halfUp
import net.kotlinx.string.toTextGrid

class LinearRegressionTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("LinearRegression") {

            val inputDatas = listOf(1L, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            val outputDatas = listOf(25L, 35, 49, 60, 75, 90, 115, 130, 150, 200)

            /** 이걸 예측하려고함 */
            val predicData = listOf(2.5, 12.0)

            Then("간단 선형회귀") {
                printName()
                val reg = LinearRegression(inputDatas, outputDatas)
                val newInputDatas = inputDatas.map { it.toDouble() } + predicData
                listOf("입력", "결과").toTextGrid(newInputDatas.sorted().map { arrayOf(it, reg[it].halfUp(2)) }).print()
            }

            (1..2).forEach { degree ->
                //1차는 선형과 동일한값, 2차는 좀저 정교한 값이 나와야 한다
                Then("참고용! 다항 회귀 ${degree}차원") {
                    printName()
                    val x = inputDatas.map { it.toDouble() }
                    val y = outputDatas.map { it.toDouble() }
                    val regression = PolynomialRegression(x.toDoubleArray(), y.toDoubleArray(), degree)
                    val newInputDatas2 = x.map { it } + predicData
                    listOf("입력", "결과").toTextGrid(newInputDatas2.sorted().map { arrayOf(it, regression.predict(it).halfUp(2)) }).print()
                }
            }

        }
    }


}