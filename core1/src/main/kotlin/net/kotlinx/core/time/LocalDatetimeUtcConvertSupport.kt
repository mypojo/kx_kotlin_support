package net.kotlinx.core.time

import java.time.LocalDateTime


/** ISO_INSTANT 로 변환 (ZonedDateTime 노출 xx) */
inline fun LocalDateTime.toUtc(): String = TimeFormat.ISO_INSTANT[this.toZone()]

/** ISO_INSTANT 로 변환 (ZonedDateTime 노출 xx) */
inline fun String.fromUtc(): LocalDateTime = TimeFormat.ISO_INSTANT.toZonedDateTime(this).toLocalDateTime()
