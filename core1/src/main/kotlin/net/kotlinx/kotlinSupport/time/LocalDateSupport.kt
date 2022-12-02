package net.kotlinx.kotlinSupport.time

import java.time.LocalDate

/** YMD  */
inline fun LocalDate.toYmd():String = TimeFormat.YMD[this]
inline fun LocalDate.toYmdF01():String = TimeFormat.YMD_F01[this]

