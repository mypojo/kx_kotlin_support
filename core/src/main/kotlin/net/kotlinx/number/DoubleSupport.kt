package net.kotlinx.number

import java.math.BigDecimal

/**
 * @see BigDecimal.halfUp
 *  */
fun Double.halfUp(scale: Int): BigDecimal = this.toBigDecimal2().halfUp(scale)

/**
 * 간단한 로그 처리용
 * avg 등에서 NaN 이 리턴될경우 처리
 *  */
fun Double.toBigDecimal2(): BigDecimal {
    if (this.isNaN()) return BigDecimal.ZERO
    return this.toBigDecimal()
}