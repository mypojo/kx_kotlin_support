package net.kotlinx.core1.string

import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
val snakeRegex = "_[a-zA-Z]".toRegex()

/** 스네이크 -> 카멜 */
fun String.toSnakeFromCamel(): String = camelRegex.replace(this) { "_${it.value}" }.lowercase(Locale.getDefault())

/** 카멜 -> 스테이크 */
fun String.toCamelFromSnake(): String = snakeRegex.replace(this) { it.value.replace("_", "").uppercase(Locale.getDefault()) }

/** 단어수 줄이기 (간이 출력용) */
fun String.abbr(size: Int, suff: String = ".."): String = if (this.length <= size) this else "${this.substring(0, kotlin.math.min(this.length, size))}$suff"


/** URL 인코딩 */
fun String.encodeUrl(): String = URLEncoder.encode(this, "UTF-8")

/** URL 디코딩 */
fun String.decodeUrl(): String = URLDecoder.decode(this, "UTF-8")