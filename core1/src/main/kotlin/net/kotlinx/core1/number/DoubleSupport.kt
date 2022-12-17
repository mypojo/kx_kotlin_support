package net.kotlinx.core1.number

import java.math.BigDecimal
import java.math.RoundingMode

/** 반올림 */
inline fun Double.toRoundUp(scale:Int):BigDecimal = this.toBigDecimal().setScale(scale, RoundingMode.HALF_UP)

