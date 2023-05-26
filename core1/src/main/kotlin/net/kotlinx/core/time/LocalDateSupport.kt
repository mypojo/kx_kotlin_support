package net.kotlinx.core.time

import java.time.LocalDate

/** YMD  */
inline fun LocalDate.toYmd():String = TimeFormat.YMD[this]
/** Y-M-D (파라메터 입력용으로 많이 씀) */
inline fun LocalDate.toYmdF01():String = TimeFormat.YMD_F01[this]

