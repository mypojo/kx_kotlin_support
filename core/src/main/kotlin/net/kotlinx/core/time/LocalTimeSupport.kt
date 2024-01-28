package net.kotlinx.core.time

import java.time.LocalTime

/** YMD  */
fun LocalTime.toHmsF01():String = TimeFormat.HMS_F01[this]
fun LocalTime.toH():String = TimeFormat.H[this]
fun LocalTime.toHm():String = TimeFormat.HM[this]
fun LocalTime.toHms():String = TimeFormat.HMS[this]