package net.kotlinx.core1.string


inline fun String.space(): Int  = this.toCharArray().sumOf { it.space() }