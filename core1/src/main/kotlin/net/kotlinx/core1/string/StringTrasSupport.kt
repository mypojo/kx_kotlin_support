package net.kotlinx.core1.string

val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
val snakeRegex = "_[a-zA-Z]".toRegex()

/** 스네이크 -> 카멜 */
inline fun String.toSnakeFromCamel(): String = camelRegex.replace(this) { "_${it.value}" }.toLowerCase()
/** 카멜 -> 스테이크 */
inline fun String.toCamelFromSnake(): String = snakeRegex.replace(this) { it.value.replace("_", "").toUpperCase() }
/** 단어수 줄이기 (간이 출력용) */
inline fun String.abbr(size: Int): String = if(this.length <= size) this else this.substring(0, kotlin.math.min(this.length, size))+".."
