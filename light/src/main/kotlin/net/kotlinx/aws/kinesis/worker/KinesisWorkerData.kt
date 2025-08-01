package net.kotlinx.aws.kinesis.worker

import aws.sdk.kotlin.services.kinesis.model.Record
import net.kotlinx.aws.kinesis.reader.gson

data class KinesisWorkerData(val record: Record) {

    /** 이 결과를 다시 write함 */
    val result = record.gson

    val taskName: String = record.partitionKey!!.substringBefore("-")
    val taskId: String = record.partitionKey!!.substringAfter("-").substringBefore("-")
    val resultPartitionKey by lazy { "$taskName-$taskId-out" }

}