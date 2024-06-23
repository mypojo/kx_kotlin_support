package net.kotlinx.string

import net.kotlinx.core.PackageNameSupport
import net.kotlinx.number.halfUp
import net.kotlinx.regex.RegexSet
import java.math.BigDecimal

object StringNumberSupport : PackageNameSupport

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