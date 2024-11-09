package net.kotlinx.aws.firehose

import aws.sdk.kotlin.services.firehose.FirehoseClient
import com.google.gson.Gson
import net.kotlinx.core.Kdsl
import net.kotlinx.json.gson.GsonSet

/**
 * 파이어호스에 다이렉트로 입력하는 간단 로거
 */
class FirehoseLogger {

    @Kdsl
    constructor(block: FirehoseLogger.() -> Unit = {}) {
        apply(block)
    }

    /** 클라이언트 */
    lateinit var firehose: FirehoseClient

    /** 스트림 명 */
    lateinit var streamName: String

    /** 기본 변환기 */
    var gson: Gson = GsonSet.TABLE_UTC_WITH_ZONE

    suspend fun putRecord(data: Any) {
        val json = gson.toJson(data)
        firehose.putRecord(streamName, json)
    }

    suspend fun putRecordBatch(data: List<Any>) {
        val jsons = data.map { gson.toJson(it) }
        firehose.putRecordBatch(streamName, jsons)
    }

}