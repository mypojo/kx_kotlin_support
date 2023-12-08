package net.kotlinx.core.number

/** 시간, 분 등의 간단 패딩 */
inline fun Number.padStart(length: Int, padChar: Char = '0'): String = this.toString().padStart(length, padChar)