package net.kotlinx.aws.kinesis.worker

import aws.sdk.kotlin.services.kinesis.model.Record
import net.kotlinx.aws.kinesis.reader.gson

data class KinesisTaskRecord(val record: Record) {

    /** 이 결과를 다시 write함 */
    val result = record.gson

    /** task 레코드 */
    val partitonKey: KinesisTaskRecordKey = KinesisTaskRecordKey.parse(record.partitionKey!!)


}