package net.kotlinx.math

import net.kotlinx.core.number.halfUp
import net.kotlinx.core.string.toTextGrid
import net.kotlinx.kotest.BeSpecLog
import org.junit.jupiter.api.Test

class PolynomialRegressionTest : BeSpecLog(){
    init {
        @Test
        fun estimate() {

            val x = listOf(1.0, 2.0, 3.0, 4.0, 5.0,10.0)
            val y = listOf(1738980.0, 874659.0, 501707.0, 279343.0, 86094.0,0.0)

            val regression = PolynomialRegression(x.toDoubleArray(), y.toDoubleArray(), 2)

            val newInputDatas = x.map { it } + listOf(2.1,3.5,7.7)

            listOf("입력", "결과").toTextGrid(newInputDatas.sorted().map { arrayOf(it, regression.predict(it).halfUp(2)) }).print()

        }


        @Test
        fun test() {


            val x = listOf(10.0, 20.0, 40.0, 80.0, 160.0, 200.0)
            val y = listOf(100.0, 350.0, 1500.0, 6700.0, 20160.0, 40000.0)

            val regression = PolynomialRegression(x.toDoubleArray(), y.toDoubleArray(), 2)

            val newInputDatas = x.map { it } + listOf(8.0,10.5,180.0,240.0)

            listOf("입력", "결과").toTextGrid(newInputDatas.sorted().map { arrayOf(it, regression.predict(it).halfUp(2)) }).print()
        }
    }
}