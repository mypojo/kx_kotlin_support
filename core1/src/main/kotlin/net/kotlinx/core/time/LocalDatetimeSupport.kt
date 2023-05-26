package net.kotlinx.core.time

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


/** 한국 시간으로 포매팅 (로그 확인용) - 기본 시분초까지 */
inline fun LocalDateTime.toKr01(): String = TimeFormat.YMDHMS_K01[this]

/** 한국 시간으로 포매팅 (로그 확인용) - 시분까지 */
inline fun LocalDateTime.toYmdhmKr01(): String = TimeFormat.YMDHM_K01[this]

/** 한국 시간으로 포매팅 (로그 확인용) - 기본 시분초까지 */
inline fun LocalDateTime.toIos(): String = TimeFormat.ISO[this]

/** YMD  */
inline fun LocalDateTime.toYmd(): String = TimeFormat.YMD[this]

/** 이게 베이스 */
inline fun LocalDateTime.toInstant(zoneId: ZoneId = TimeUtil.SEOUL): Instant = this.atZone(zoneId).toInstant()

/** 한국시간 기준 미리초 리턴 */
fun LocalDateTime.toLong(zoneId: ZoneId = TimeUtil.SEOUL): Long = this.atZone(zoneId).toInstant().toEpochMilli()

/** Date 로 컴버팅*/
fun LocalDateTime.toDate(zoneId: ZoneId = TimeUtil.SEOUL): Date = Date.from(this.atZone(zoneId).toInstant())

