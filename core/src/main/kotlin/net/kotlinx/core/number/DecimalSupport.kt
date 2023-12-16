package net.kotlinx.core.number

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 반올림
 * ex) 비율을 구하는 경우 ( A * 100.0 / B).toRate(1)
 *  */
fun BigDecimal.halfUp(scale: Int): BigDecimal = this.setScale(scale, RoundingMode.HALF_UP)


/**
 * 기본 div는 이미 있음
 * 반드시 this.setScale(scale) 후 호출할것!!
 *  */
fun BigDecimal.div2(value: BigDecimal): BigDecimal {
    if (value == BigDecimal.ZERO) return BigDecimal.ZERO
    return this / value
}