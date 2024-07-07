package net.kotlinx.time

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


//==================================================== ktor internal 에서 일부 가져옴 ======================================================

fun Date.toLocalDateTime(zone: ZoneId = TimeUtil.SEOUL): LocalDateTime = LocalDateTime.ofInstant(toInstant(), zone)