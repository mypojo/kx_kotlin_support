package net.kotlinx.aws.sqs

import aws.sdk.kotlin.services.sqs.model.Message
import aws.sdk.kotlin.services.sqs.model.MessageSystemAttributeName
import net.kotlinx.number.toLocalDateTime
import java.time.LocalDateTime

/** 간단 시간 변환 */
val Message.sentTimestamp: LocalDateTime
    get() = this.attributes!![MessageSystemAttributeName.SentTimestamp]!!.toLong().toLocalDateTime()