package net.kotlinx.core.number

import java.math.BigDecimal

/**
 * @see BigDecimal.halfUp
 *  */
inline fun Double.halfUp(scale: Int): BigDecimal = this.toBigDecimal().halfUp(scale)
