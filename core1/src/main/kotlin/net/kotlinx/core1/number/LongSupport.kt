package net.kotlinx.core1.number

import net.kotlinx.core1.time.TimeString

/** 밀리초로 간주하고 문자로 변환 (로그 확인용) */
inline fun Long.toTimeString():TimeString = TimeString(this)