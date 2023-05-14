package net.kotlinx.core1.time

import java.time.LocalDateTime
import java.time.ZonedDateTime


/** 기본 존으로 변환 */
inline fun LocalDateTime.toZone(): ZonedDateTime = this.atZone(TimeUtil.SEOUL)

/** ISO_INSTANT 로 변환 (ZonedDateTime 노출 xx) */
inline fun LocalDateTime.toUtc(): String = TimeFormat.ISO_INSTANT[this.toZone()]

/** ISO_INSTANT 로 변환 (ZonedDateTime 노출 xx) */
inline fun String.fromUtc(): LocalDateTime = TimeFormat.ISO_INSTANT.toZonedDateTime(this).toLocalDateTime()
