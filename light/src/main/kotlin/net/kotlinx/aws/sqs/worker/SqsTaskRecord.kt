package net.kotlinx.aws.sqs.worker

import aws.sdk.kotlin.services.sqs.model.Message
import net.kotlinx.json.gson.GsonData

/**
 * SQS 메시지를 래핑하는 클래스
 */
data class SqsTaskRecord(val message: Message) {

    /** 이 결과를 다시 write함 */
    val result = GsonData.parse(message.body!!)

    /** task 레코드 */
    val messageKey: SqsTaskRecordKey = SqsTaskRecordKey.parse(message.messageId!!)

}