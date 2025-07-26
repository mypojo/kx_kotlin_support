package net.kotlinx.string

import net.kotlinx.core.PackageNameSupport
import net.kotlinx.number.halfUp
import net.kotlinx.number.padStart
import net.kotlinx.regex.RegexSet
import net.kotlinx.regex.retainFrom
import java.math.BigDecimal
import java.math.MathContext

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
 * 간단 숫자변환.
 * 뭐든지 일단 숫자로 바꿔줄 용도로 제작되었다.
 * 로그등을 간단히 파싱하려는 목적
 */
fun String.toBigDecimal2(): BigDecimal {
    //엑셀 등에서 로그 형식으로 변경된 문자의 경우
    val text = this
    try {
        if (text.contains("E", ignoreCase = true)) {
            val parts = text.split("E", ignoreCase = true)
            check(parts.size == 2)
            val base = BigDecimal(parts[0])
            val exponent = parts[1].toInt()
            return base.multiply(BigDecimal.TEN.pow(exponent, MathContext.DECIMAL128))
        }
    } catch (e: Exception) {
        //변환을 무시한다ㅣ
    }
    return this.retainFrom(RegexSet.NUMERIC_DOT).toBigDecimal()
}

/**
 * 문자열 뒤에 특정 숫자 일련번호를 붙인다
 * ex) aa.result ->  aa.result-r001
 * ex) aa.result-r001 -> aa.result-r002
 * */
fun String.padNumIncrease(prefix: String = "-R", pad: Int = 3, append: Int = 1): String {
    val format = Regex("""^(.*)${prefix}(\d+)$""")
    val matchResult = format.find(this)
    return if (matchResult != null) {
        val (body, num) = matchResult.destructured
        val increasedNum = num.toInt() + append
        "$body${prefix}${increasedNum.toString().padStart(pad, '0')}"
    } else {
        "$this${prefix}${1.padStart(pad, '0')}"
    }
}