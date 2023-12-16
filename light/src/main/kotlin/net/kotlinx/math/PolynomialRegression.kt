package net.kotlinx.math

import Jama.Matrix
import Jama.QRDecomposition
import mu.KotlinLogging
import net.kotlinx.core.number.halfUp
import kotlin.math.abs
import kotlin.math.pow

/**
 * https://algs4.cs.princeton.edu/14analysis/PolynomialRegression.java.html
 * 여기서 퍼왔어요~
 *
 *
 * The `PolynomialRegression` class performs a polynomial regression
 * on an set of *N* data points (*y<sub>i</sub>*, *x<sub>i</sub>*).
 * That is, it fits a polynomial
 * *y* = <sub>0</sub> +  <sub>1</sub> *x* +
 * <sub>2</sub> *x*<sup>2</sup> + ... +
 * <sub>*d*</sub> *x*<sup>*d*</sup>
 * (where *y* is the response variable, *x* is the predictor variable,
 * and the <sub>*i*</sub> are the regression coefficients)
 * that minimizes the sum of squared residuals of the multiple regression model.
 * It also computes associated the coefficient of determination *R*<sup>2</sup>.
 *
 *
 * This implementation performs a QR-decomposition of the underlying
 * Vandermonde matrix, so it is neither the fastest nor the most numerically
 * stable way to perform the polynomial regression.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
@Suppress("KDocUnresolvedReference")
class PolynomialRegression(
    x: DoubleArray, y: DoubleArray, // degree of the polynomial regression
    private var degree: Int, // name of the predictor variable
    private val variableName: String = "n"
) {

    private val beta: Matrix // the polynomial regression coefficients
    private val sse: Double // sum of squares due to error
    private var sst = 0.0 // total sum of squares

    /**
     * Performs a polynomial reggression on the data points `(y[i], x[i])`.
     *
     * @param  x the values of the predictor variable
     * @param  y the corresponding values of the response variable
     * @param  degree the degree of the polynomial to fit
     * @param  variableName the name of the predictor variable
     * @throws IllegalArgumentException if the lengths of the two arrays are not equal
     */
    /**
     * Performs a polynomial reggression on the data points `(y[i], x[i])`.
     * Uses n as the name of the predictor variable.
     *
     * @param  x the values of the predictor variable
     * @param  y the corresponding values of the response variable
     * @param  degree the degree of the polynomial to fit
     * @throws IllegalArgumentException if the lengths of the two arrays are not equal
     */
    init {

        val n = x.size
        var qr: QRDecomposition?
        var matrixX: Matrix?

        // in case Vandermonde matrix does not have full rank, reduce degree until it does
        while (true) {
            // build Vandermonde matrix

            val vandermonde = Array(n) { DoubleArray(this.degree + 1) }
            for (i in 0 until n) {
                for (j in 0..this.degree) {
                    vandermonde[i][j] = x[i].pow(j.toDouble())
                }
            }
            matrixX = Matrix(vandermonde)

            // find least squares solution
            qr = QRDecomposition(matrixX)
            if (qr.isFullRank) break

            // decrease degree and try again
            degree--
        }

        // create matrix from vector
        val matrixY = Matrix(y, n)

        // linear regression coefficients
        beta = qr!!.solve(matrixY)

        // mean of y[] values
        var sum = 0.0
        for (i in 0 until n) sum += y[i]
        val mean = sum / n

        // total variation to be accounted for
        for (i in 0 until n) {
            val dev = y[i] - mean
            sst += dev * dev
        }

        // variation not accounted for
        val residuals: Matrix = matrixX!!.times(beta).minus(matrixY)
        sse = residuals.norm2() * residuals.norm2()
    }

    /**
     * Returns the `j`th regression coefficient.
     *
     * @param  j the index
     * @return the `j`th regression coefficient
     */
    fun beta(j: Int): Double {
        // to make -0.0 print as 0.0
        if (abs(beta.get(j, 0)) < 1E-4) return 0.0
        return beta.get(j, 0)
    }

    /**
     * Returns the degree of the polynomial to fit.
     *
     * @return the degree of the polynomial to fit
     */
    fun degree(): Int {
        return degree
    }

    /**
     * Returns the coefficient of determination *R*<sup>2</sup>.
     *
     * @return the coefficient of determination *R*<sup>2</sup>,
     * which is a real number between 0 and 1
     */
    fun r2(): Double {
        if (sst == 0.0) return 1.0 // constant function

        return 1.0 - sse / sst
    }

    /**
     * Returns the expected response `y` given the value of the predictor
     * variable `x`.
     *
     * @param  x the value of the predictor variable
     * @return the expected response `y` given the value of the predictor
     * variable `x`
     */
    fun predict(x: Double): Double {
        // horner's method
        var y = 0.0
        for (j in degree downTo 0) y = beta(j) + (x * y)
        return y
    }

    /**
     * Returns a string representation of the polynomial regression model.
     *
     * @return a string representation of the polynomial regression model,
     * including the best-fit polynomial and the coefficient of
     * determination *R*<sup>2</sup>
     */
    override fun toString(): String {
        var s = StringBuilder()
        var j = degree

        // ignoring leading zero coefficients
        while (j >= 0 && abs(beta(j)) < 1E-5) j--

        // create remaining terms
        while (j >= 0) {
            when (j) {
                0 -> s.append(String.format("%.2f ", beta(j)))
                1 -> s.append(String.format("%.2f %s + ", beta(j), variableName))
                else -> s.append(String.format("%.2f %s^%d + ", beta(j), variableName, j))
            }
            j--
        }
        s = s.append("  (R^2 = " + String.format("%.3f", r2()) + ")")

        // replace "+ -2n" with "- 2n"
        return s.toString().replace("+ -", "- ")
    }

    companion object {

        private val log = KotlinLogging.logger {}

        /** 간단하게 더 맞는걸 찾아준다. */
        fun findFit(x: List<Number>, y: List<Number>): PolynomialRegression {
            val xx = x.map { it.toDouble() }.toDoubleArray()
            val yy = y.map { it.toDouble() }.toDoubleArray()
            val degree1 = PolynomialRegression(xx, yy, 1)
            val degree2 = PolynomialRegression(xx, yy, 2)
            log.debug { " => r2 diff -> d1 ${(degree1.r2() * 100).halfUp(1)} vs d2 ${(degree2.r2() * 100).halfUp(1)}" }
            return listOf(degree1, degree2).maxBy { it.r2() }
        }

    }

}