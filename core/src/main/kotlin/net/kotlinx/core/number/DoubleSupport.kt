package net.kotlinx.core.number

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 반올림
 * ex) 비율을 구하는 경우 ( A * 100.0 / B).toRate(1)
 *  */
inline fun Double.toRoundUp(scale: Int): BigDecimal = this.toBigDecimal().setScale(scale, RoundingMode.HALF_UP)
