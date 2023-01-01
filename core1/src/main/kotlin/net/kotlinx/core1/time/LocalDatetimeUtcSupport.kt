package net.kotlinx.core1.time

import java.time.LocalDateTime
import java.time.ZonedDateTime


/** ISO_INSTANT 로 변환 */
inline fun LocalDateTime.toUtc(): String = TimeUtil.ISO_INSTANT.format(this.atZone(TimeUtil.SEOUL))

/** ISO_INSTANT 로 변환 */
inline fun String.fromUtc(): LocalDateTime = ZonedDateTime.from(TimeUtil.ISO_INSTANT.parse(this)).toLocalDateTime()