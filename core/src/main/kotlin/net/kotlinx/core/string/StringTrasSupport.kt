package net.kotlinx.core.string

import net.kotlinx.core.regex.RegexSet
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*


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


/** URL 인코딩 */
fun String.encodeUrl(): String = URLEncoder.encode(this, "UTF-8")

/** URL 디코딩 */
fun String.decodeUrl(): String = URLDecoder.decode(this, "UTF-8")

/** 전체가 숫자인지? 간단 테스트 메소드 */
inline fun String.isNumeric(): Boolean = RegexSet.NUMERIC.matches(this)