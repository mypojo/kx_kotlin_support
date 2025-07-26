package net.kotlinx.aws.kinesis

import aws.sdk.kotlin.services.kinesis.KinesisClient
import aws.sdk.kotlin.services.kinesis.model.PutRecordResponse
import aws.sdk.kotlin.services.kinesis.model.PutRecordsRequestEntry
import aws.sdk.kotlin.services.kinesis.model.PutRecordsResponse
import aws.sdk.kotlin.services.kinesis.putRecord
import aws.sdk.kotlin.services.kinesis.putRecords
import com.google.gson.Gson
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist
import net.kotlinx.id.Identity
import net.kotlinx.json.gson.GsonSet

val AwsClient.kinesis: KinesisClient
    get() = getOrCreateClient { KinesisClient { awsConfig.build(this) }.regist(awsConfig) }

/** 객체(data class) 입력 샘플 */
suspend fun <T : Identity<out Any>> KinesisClient.putRecord(streamName: String, data: T, gson: Gson = GsonSet.TABLE_UTC_WITH_ZONE): PutRecordResponse = this.putRecord {
    this.streamName = streamName
    this.partitionKey = data.id.toString()
    this.data = gson.toJson(data)!!.toByteArray()
}

/**
 * 다수의 객체(data class) 입력 샘플
 * @see KinesisWriter
 * */
@Deprecated("사용안함 KinesisWriter 쓰세요")
suspend fun <T : Identity<out Any>> KinesisClient.putRecords(streamName: String, datas: List<T>, gson: Gson = GsonSet.TABLE_UTC_WITH_ZONE): PutRecordsResponse = this.putRecords {
    this.streamName = streamName
    this.records = datas.map { data ->
        PutRecordsRequestEntry {
            this.partitionKey = data.id.toString()
            this.data = gson.toJson(data)!!.toByteArray()
        }
    }
}




