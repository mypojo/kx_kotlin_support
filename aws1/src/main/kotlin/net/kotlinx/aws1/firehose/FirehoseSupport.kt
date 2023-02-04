package net.kotlinx.aws1.firehose

import aws.sdk.kotlin.services.firehose.FirehoseClient
import aws.sdk.kotlin.services.firehose.model.PutRecordBatchResponse
import aws.sdk.kotlin.services.firehose.model.PutRecordResponse
import aws.sdk.kotlin.services.firehose.model.Record
import aws.sdk.kotlin.services.firehose.putRecord
import aws.sdk.kotlin.services.firehose.putRecordBatch

/** 단축 입력 인라인 */
suspend inline fun FirehoseClient.putRecord(StreamName: String, json: String): PutRecordResponse = this.putRecord {
    this.deliveryStreamName = StreamName
    this.record { this.data = json.toByteArray() }
}

/**
 * 단축 입력 인라인
 * 출당 최대 500개의 레코드 또는 호출당 4MiB 제한 있음
 * */
suspend inline fun FirehoseClient.putRecordBatch(StreamName: String, jsons: List<String>): List<PutRecordBatchResponse> {
    return jsons.chunked(500).map { splited ->
        this.putRecordBatch {
            this.deliveryStreamName = StreamName
            this.records = splited.map { Record { data = it.toByteArray() } }
        }
    }
}