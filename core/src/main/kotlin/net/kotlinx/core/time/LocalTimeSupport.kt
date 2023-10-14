package net.kotlinx.core.time

import java.time.LocalTime

/** YMD  */
inline fun LocalTime.toHmsF01():String = TimeFormat.HMS_F01[this]
inline fun LocalTime.toH():String = TimeFormat.H[this]