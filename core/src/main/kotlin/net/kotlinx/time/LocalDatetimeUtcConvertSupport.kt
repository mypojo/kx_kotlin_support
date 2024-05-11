package net.kotlinx.time

import java.time.LocalDateTime


//==================================================== ISO_INSTANT  ======================================================

/** ISO_INSTANT 로 변환 (ZonedDateTime 노출 xx) */
inline fun LocalDateTime.toUtc(): String = TimeFormat.ISO_INSTANT[this.toZone()]

/** ISO_INSTANT 로 변환 (ZonedDateTime 노출 xx) */
inline fun String.fromUtc(): LocalDateTime = TimeFormat.ISO_INSTANT.toZonedDateTime(this).toLocalDateTime()

//==================================================== ISO_OFFSET ======================================================


/** ISO_INSTANT 로 변환 (ZonedDateTime 노출) */
inline fun LocalDateTime.toUtcZone(): String = TimeFormat.ISO_OFFSET[this.toZone()]

/** ISO_INSTANT 로 변환 (ZonedDateTime 노출) */
inline fun String.fromUtcZone(): LocalDateTime = TimeFormat.ISO_OFFSET.toZonedDateTime(this).toLocalDateTime()