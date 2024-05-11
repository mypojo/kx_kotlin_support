package net.kotlinx.aws

import aws.smithy.kotlin.runtime.time.Instant
import net.kotlinx.string.toLocalDateTime
import java.time.LocalDateTime

/** 한국시간 Local 로 변환 */
fun Instant.toLocalDateTime(): LocalDateTime = this.toString().toLocalDateTime().plusHours(9)