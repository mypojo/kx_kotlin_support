package net.kotlinx.aws1

import aws.smithy.kotlin.runtime.time.Instant
import net.kotlinx.core1.string.toLocalDateTime
import java.time.LocalDateTime


/** 한국시간 Local 로 변환 */
inline fun Instant.toLocalDateTime():LocalDateTime = this.toString().toLocalDateTime().plusHours(9)
