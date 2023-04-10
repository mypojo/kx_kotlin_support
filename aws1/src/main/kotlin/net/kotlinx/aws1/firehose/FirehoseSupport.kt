package net.kotlinx.aws1.firehose

import aws.sdk.kotlin.services.firehose.FirehoseClient
import aws.sdk.kotlin.services.firehose.model.PutRecordBatchResponse
import aws.sdk.kotlin.services.firehose.model.PutRecordResponse
import aws.sdk.kotlin.services.firehose.model.Record
import aws.sdk.kotlin.services.firehose.putRecord
import aws.sdk.kotlin.services.firehose.putRecordBatch

/** 단축 입력 인라인 */
suspend inline fun FirehoseClient.putRecord(streamName: String, json: String): PutRecordResponse = this.putRecord {
    this.deliveryStreamName = streamName
    this.record { this.data = json.toByteArray() }
}

/**
 * 단축 입력 인라인
 * API 리미트 : 출당 최대 500개의 레코드 또는 호출당 4MiB 제한 있음
 *
 * 참고로 각 계정은 리전당 최대 50개의 Kinesis Data Firehose 전송 스트림 보유 가능.
 * 스트림당 소프트 리미트 : 초당 레코드 100,000개, 초당 요청 1,000개, 초당 1MiB.
 * 요금 : 5KB(5120바이트) 단위로 반올림한 값을 기반
 * */
suspend inline fun FirehoseClient.putRecordBatch(streamName: String, jsons: List<String>): List<PutRecordBatchResponse> {
    return jsons.chunked(500).map { splited ->
        this.putRecordBatch {
            this.deliveryStreamName = streamName
            this.records = splited.map { Record { data = it.toByteArray() } }
        }
    }
}