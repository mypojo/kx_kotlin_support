package net.kotlinx.aws1.s3

import aws.sdk.kotlin.services.firehose.FirehoseClient
import aws.sdk.kotlin.services.firehose.model.PutRecordResponse
import aws.sdk.kotlin.services.firehose.putRecord

/** 단축 입력 인라인 */
suspend inline fun FirehoseClient.putRecord(StreamName: String, json: String): PutRecordResponse = this.putRecord {
    this.deliveryStreamName = StreamName
    this.record { this.data = json.toByteArray() }
}