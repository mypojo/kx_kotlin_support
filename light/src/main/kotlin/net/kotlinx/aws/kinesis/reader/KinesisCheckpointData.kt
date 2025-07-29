package net.kotlinx.aws.kinesis.reader

import kotlinx.serialization.Serializable

/**
 * 체크포인트 데이터 클래스
 *
 * @property shardId 샤드 ID
 * @property sequenceNumber 마지막으로 처리된 레코드의 시퀀스 번호
 * @property subSequenceNumber 마지막으로 처리된 레코드의 하위 시퀀스 번호
 * @property lastUpdateTime 마지막 업데이트 시간 (밀리초)
 */
@Serializable
data class KinesisCheckpointData(
    val shardId: String,
    val sequenceNumber: String,
    val subSequenceNumber: Long = 0,
    val lastUpdateTime: Long = System.currentTimeMillis()
)