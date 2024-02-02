package net.kotlinx.core.string

import net.kotlinx.core.number.halfUp
import net.kotlinx.core.regex.RegexSet
import java.math.BigDecimal


/**
 * 간단 숫자변환
 * 로그등을 간단히 파싱하려는 목적
 *  */
fun String.toLong2(): Long = try {
    this.toLong()
} catch (e: NumberFormatException) {
    this.toBigDecimal().halfUp(0).toLong()
}

/**
 * 간단 숫자변환
 * 로그등을 간단히 파싱하려는 목적
 */
fun String.toBigDecimal2(): BigDecimal = this.retainFrom(RegexSet.NUMERIC_DOT).toBigDecimal()