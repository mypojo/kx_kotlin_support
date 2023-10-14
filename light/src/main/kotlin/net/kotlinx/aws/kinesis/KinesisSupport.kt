package net.kotlinx.aws.kinesis

import aws.sdk.kotlin.services.kinesis.KinesisClient
import aws.sdk.kotlin.services.kinesis.model.PutRecordResponse
import aws.sdk.kotlin.services.kinesis.model.PutRecordsRequestEntry
import aws.sdk.kotlin.services.kinesis.model.PutRecordsResponse
import aws.sdk.kotlin.services.kinesis.putRecord
import aws.sdk.kotlin.services.kinesis.putRecords
import com.google.gson.Gson
import net.kotlinx.core.Identity
import net.kotlinx.core.gson.GsonSet

/** 객체(data class) 입력 샘플 */
suspend fun <T : Identity<out Any>> KinesisClient.putRecord(streamName: String, data: T, gson: Gson = GsonSet.TABLE_UTC): PutRecordResponse = this.putRecord {
    this.streamName = streamName
    this.partitionKey = data.id.toString()
    this.data = gson.toJson(data)!!.toByteArray()
}

/** 다수의 객체(data class) 입력 샘플 */
suspend fun <T : Identity<out Any>> KinesisClient.putRecords(streamName: String, datas: List<T>, gson: Gson = GsonSet.TABLE_UTC): PutRecordsResponse = this.putRecords {
    this.streamName = streamName
    this.records = datas.map { data ->
        PutRecordsRequestEntry {
            this.partitionKey = data.id.toString()
            this.data = gson.toJson(data)!!.toByteArray()
        }
    }
}




