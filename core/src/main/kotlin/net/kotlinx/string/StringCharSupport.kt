package net.kotlinx.string


fun String.space(): Int = this.toCharArray().sumOf { it.space() }