package net.kotlinx.math

import mu.KotlinLogging
import net.kotlinx.core.lib.SystemUtil
import net.kotlinx.core.number.toSiText
import net.kotlinx.core.time.TimeStart
import kotlin.math.pow

/**
 * 간단한 선형회귀
 * SimplePolynomialRegression 다항 회귀 (곡선) 는 힘들듯..
 *  */
class SimpleLinearRegression(private val xs: List<Int>, private val ys: List<Int>) {

    init {
        check(xs.size == ys.size)
    }

    private val start = TimeStart()

    // Variance
    private val variance = xs.sumOf { x -> (x - xs.average()).pow(2) }

    // Covariance
    private val covariance = xs.zip(ys) { x, y -> (x - xs.average()) * (y - ys.average()) }.sum()

    // Slope - 기울기 계산
    private val slope = covariance / variance

    // Y Intercept - 절편 계산
    private val yIntercept = ys.average() - slope * xs.average()

    // Simple Linear Regression
    private val simpleLinearRegression = { independentVariable: Double -> slope * independentVariable + yIntercept }

    // R² (정확도. 0.95인경우 95% 정확)
    private val rsquared = run {
        // SST
        val sst = ys.sumOf { y -> (y - ys.average()).pow(2) }
        // SSR
        val ssr = xs.zip(ys) { x, y -> (y - simpleLinearRegression.invoke(x.toDouble())).pow(2) }.sum()
        val r2 = (sst - ssr) / sst
        log.debug { "LinearRegression 정확도 $r2 -> $start / 메모리 ${SystemUtil.nowUsedMemory().toSiText()}" }
        r2
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }

    /** 추정치 가져오기 */
    operator fun get(x: Double) = simpleLinearRegression.invoke(x)

}