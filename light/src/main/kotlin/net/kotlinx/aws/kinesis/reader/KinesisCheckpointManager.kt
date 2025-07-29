package net.kotlinx.aws.kinesis.reader

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.GetItemRequest
import aws.sdk.kotlin.services.dynamodb.model.PutItemRequest
import mu.KotlinLogging
import net.kotlinx.aws.dynamo.dynamo
import net.kotlinx.aws.dynamo.enhanced.DbTable

/**
 * Kinesis 스트림의 샤드 처리 위치를 DynamoDB에 저장하고 관리하는 클래스
 */
class KinesisCheckpointManager(private val reader: KinesisReader) {

    /**
     * 체크포인트 정보를 DynamoDB에 저장
     *
     * @param data 저장할 체크포인트 데이터
     */
    suspend fun saveCheckpoint(data: KinesisCheckpointData) {
        val request = PutItemRequest {
            tableName = reader.checkpointTableName
            item = mapOf(
                DbTable.PK_NAME to AttributeValue.S(reader.readerName),
                DbTable.SK_NAME to AttributeValue.S(data.shardId),
                "SequenceNumber" to AttributeValue.S(data.sequenceNumber),
                "SubSequenceNumber" to AttributeValue.N(data.subSequenceNumber.toString()),
                "LastUpdateTime" to AttributeValue.N(data.lastUpdateTime.toString())
            )
        }
        reader.aws.dynamo.putItem(request)
        log.debug { "[${data.shardId}] 체크포인트 저장완료 -> ${data.sequenceNumber}" }
    }


    /**
     * 특정 샤드의 체크포인트 정보를 DynamoDB에서 조회
     *
     * @param shardId 조회할 샤드 ID
     * @return 체크포인트 데이터 또는 null (조회 실패 시)
     */
    suspend fun getCheckpoint(shardId: String): KinesisCheckpointData? {
        val request = GetItemRequest {
            tableName = reader.checkpointTableName
            key = mapOf(
                DbTable.PK_NAME to AttributeValue.S(reader.readerName),
                DbTable.SK_NAME to AttributeValue.S(shardId)
            )
        }

        val response = reader.aws.dynamo.getItem(request)
        return response.item?.let { item ->
            KinesisCheckpointData(
                shardId = item[DbTable.SK_NAME]?.asS() ?: shardId,
                sequenceNumber = item["SequenceNumber"]?.asS() ?: "",
                subSequenceNumber = item["SubSequenceNumber"]?.asN()?.toLongOrNull() ?: 0,
                lastUpdateTime = item["LastUpdateTime"]?.asN()?.toLongOrNull() ?: 0
            )
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }

}