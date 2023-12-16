package net.kotlinx.core.string

import net.kotlinx.core.number.halfUp


/** 간단 숫자변환 */
fun String.toLong2(): Long = try {
    this.toLong()
} catch (e: NumberFormatException) {
    this.toBigDecimal().halfUp(0).toLong()
}