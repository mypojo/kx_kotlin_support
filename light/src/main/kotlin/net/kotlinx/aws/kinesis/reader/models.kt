package net.kotlinx.aws.kinesis.reader

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Kinesis 레코드 데이터 클래스
 * 
 * @property sequenceNumber 레코드의 시퀀스 번호
 * @property partitionKey 레코드의 파티션 키
 * @property data 레코드의 데이터 (문자열)
 * @property approximateArrivalTimestamp 레코드의 대략적인 도착 시간 (epoch seconds)
 * @property shardId 레코드가 속한 샤드 ID
 */
data class KinesisRecord(
    val sequenceNumber: String,
    val partitionKey: String,
    val data: String,
    val approximateArrivalTimestamp: Long,
    val shardId: String
) {
    /**
     * 레코드의 도착 시간을 Instant 형태로 반환
     */
    val arrivalTime: Instant
        get() = Instant.ofEpochSecond(approximateArrivalTimestamp)
}

/**
 * 체크포인트 데이터 클래스
 * 
 * @property shardId 샤드 ID
 * @property sequenceNumber 마지막으로 처리된 레코드의 시퀀스 번호
 * @property subSequenceNumber 마지막으로 처리된 레코드의 하위 시퀀스 번호
 * @property lastUpdateTime 마지막 업데이트 시간 (밀리초)
 */
@Serializable
data class CheckpointData(
    val shardId: String,
    val sequenceNumber: String,
    val subSequenceNumber: Long = 0,
    val lastUpdateTime: Long = System.currentTimeMillis()
)