package net.kotlinx.kotest.mockk

import net.kotlinx.core.PackageNameSupport

object MockkTestSupport : PackageNameSupport

inline fun String.toMockkInlineString1() = "인라인1"

fun String.toMockkInlineString2() = "인라인2"
