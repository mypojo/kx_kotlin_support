package net.kotlinx.aws.kinesis.reader

import aws.sdk.kotlin.services.kinesis.model.Record
import aws.smithy.kotlin.runtime.time.toJvmInstant
import net.kotlinx.json.gson.GsonData
import net.kotlinx.string.abbr
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.time.TimeUtil
import net.kotlinx.time.toKr01
import java.time.LocalDateTime

/**
 * Record의 데이터를 문자열로 변환
 */
val Record.dataAsString: String
    get() = this.data!!.decodeToString()

/** 대부분 json 형식일듯 */
val Record.gson: GsonData
    get() = GsonData.parse(dataAsString)

/** Record의 도착 시간을 java.time.Instant 형태로 반환 */
val Record.arrivalTime: LocalDateTime
    get() = this.approximateArrivalTimestamp!!.toJvmInstant().atZone(TimeUtil.SEOUL).toLocalDateTime()

/**
 * 샤드 ID와 함께 간단 출력
 */
fun List<Record>.printSimple() {
    listOf("시퀀스번호", "파티션키", "데이터", "도착시간").toTextGridPrint {
        this.map {
            arrayOf(
                it.sequenceNumber ?: "",
                it.partitionKey ?: "",
                it.dataAsString.abbr(100),
                it.arrivalTime.toKr01(),
            )
        }
    }
}