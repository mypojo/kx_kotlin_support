package net.kotlinx.aws1.firehose

import aws.sdk.kotlin.services.firehose.FirehoseClient
import com.google.gson.Gson
import net.kotlinx.core2.gson.GsonSet

/**
 * 키네시스 간단 로거
 */
class KdfLogger(
    private val firehose: FirehoseClient,
    val streamName: String,
    private val gson: Gson = GsonSet.TABLE_UTC,
) {
    suspend fun putRecord(data: Any) {
        val json = gson.toJson(data)
        firehose.putRecord(streamName, json)
    }

    suspend fun putRecordBatch(data: List<Any>) {
        val jsons = data.map { gson.toJson(it) }
        firehose.putRecordBatch(streamName, jsons)
    }

}