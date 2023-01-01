package net.kotlinx.core1.time

import java.time.LocalDateTime


/** 한국 시간으로 포매팅 (로그 확인용) */
inline fun LocalDateTime.toKr01(): String = TimeFormat.YMDHMS_K01[this]

/** YMD  */
inline fun LocalDateTime.toYmd(): String = TimeFormat.YMD[this]

/** 한국시간 기준 미리초 리턴 */
inline fun LocalDateTime.toLong(): Long = TimeUtil.getMills(this)