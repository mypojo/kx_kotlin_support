package net.kotlinx.core.string


inline fun String.space(): Int = this.toCharArray().sumOf { it.space() }