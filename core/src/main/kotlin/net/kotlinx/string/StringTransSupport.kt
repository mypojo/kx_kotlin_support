package net.kotlinx.string

import net.kotlinx.regex.RegexSet
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

/**
 * map 의 내용을 전부 replace 한다
 * 주로 쿼리 등을 치환할때 사용함
 *  */
fun String.replaceAll(replacements: Map<String, String>): String {
    var result = this
    replacements.entries.forEach { result = result.replace(it.key, it.value) }
    return result
}

/**
 * SQL 형태로 치환한다
 * */
fun String.replaceSqlAll(replacements: Map<String, Any?>): String {
    var result = this
    replacements.entries.forEach { e ->
        val replaced = when (val value = e.value) {
            null -> "NULL"
            is Number -> value.toString()
            is Boolean -> if (value) "TRUE" else "FALSE"
            else -> "'${value.toString().replace("'", "''")}'" // SQL 인젝션 방지
        }
        result = result.replace(e.key, replaced)
    }
    return result
}

/** 간단 마스킹 */
fun String.mask(start: Int = 2, end: Int = this.length): String {
    val input = this
    if (start < 0 || end > input.length || start > end) {
        throw IllegalArgumentException("Invalid start or end position")
    }
    val maskLength = end - start
    val mask = "*".repeat(maskLength)
    return input.substring(0, start) + mask + input.substring(end)
}

/** decapitalize 쓰고싶은데 왜!!! 부활시킴 */
fun String.decapital(): String = this.replaceFirstChar { it.lowercase(Locale.getDefault()) }

/** capital 쓰고싶은데 왜!!! 부활시킴 */
fun String.capital(): String = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

/** 스네이크 -> 카멜 */
fun String.toSnakeFromCamel(): String = RegexSet.CAMEL.replace(this) { "_${it.value}" }.lowercase(Locale.getDefault())

/** 카멜 -> 스테이크 */
fun String.toCamelFromSnake(): String = RegexSet.SNAKE.replace(this) { it.value.replace("_", "").uppercase(Locale.getDefault()) }

/** 단어수 줄이기 (간이 출력용) */
fun String.abbr(size: Int, suff: String = ".."): String = if (this.length <= size) this else "${this.substring(0, kotlin.math.min(this.length, size))}$suff"


/** 전체가 숫자인지? 간단 테스트 메소드 */
fun String.isNumeric(): Boolean = RegexSet.NUMERIC.matches(this)

//==================================================== 인코딩 ======================================================

/** URL 인코딩 */
fun String.encodeUrl(): String = URLEncoder.encode(this, "UTF-8")

/** URL 디코딩 */
fun String.decodeUrl(): String = URLDecoder.decode(this, "UTF-8")

/** Base64 인코딩 */
fun String.encodeBase64(): String = Base64.getEncoder().encodeToString(this.toByteArray())

/** Base64 디코딩 */
fun String.decodeBase64(): String = String(Base64.getDecoder().decode(this))


//==================================================== 프리미티브 변환 ======================================================

/**
 * @return boolean변환.  변환이 안될경우 null
 *  */
fun String.toBoolean(): Boolean? {
    val value = this.lowercase()
    return when (value) {
        in setOf("yes", "true", "on", "1", "t", "o") -> true
        in setOf("no", "false", "off", "0", "f", "x") -> false
        else -> null
    }
}