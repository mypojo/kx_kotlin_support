package net.kotlinx.aws.kinesis.reader

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.*
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.LazyAwsClientProperty
import net.kotlinx.core.Kdsl

/**
 * Kinesis 스트림의 샤드 처리 위치를 DynamoDB에 저장하고 관리하는 클래스
 */
class CheckpointManager {

    @Kdsl
    constructor(block: CheckpointManager.() -> Unit = {}) {
        apply(block)
    }


    //==================================================== 설정 ======================================================

    /** aws 클라이언트 */
    var aws: AwsClient by LazyAwsClientProperty()

    /** 체크포인트 정보를 저장할 DynamoDB 테이블 이름 */
    lateinit var tableName: String

    /** 애플리케이션 이름 (파티션 키로 사용) */
    lateinit var applicationName: String

    /** AWS 리전 (기본값: ap-northeast-2) */
    var region: String = "ap-northeast-2"

    //==================================================== 내부 상태 ======================================================

    private val dynamoClient by lazy { 
        DynamoDbClient { 
            this.region = this@CheckpointManager.region 
        }
    }
    
    companion object {
        private val log = KotlinLogging.logger {}
    }
    
    //==================================================== 기능 ======================================================
    
    /**
     * 체크포인트 테이블이 존재하지 않으면 생성
     */
    suspend fun createTableIfNotExists() {
        try {
            val request = DescribeTableRequest {
                tableName = this@CheckpointManager.tableName
            }
            dynamoClient.describeTable(request)
            log.info { "체크포인트 테이블이 이미 존재합니다: $tableName" }
        } catch (e: Exception) {
            log.info { "체크포인트 테이블을 생성합니다: $tableName" }
            val request = CreateTableRequest {
                tableName = this@CheckpointManager.tableName
                keySchema = listOf(
                    KeySchemaElement {
                        attributeName = "ApplicationName"
                        keyType = KeyType.Hash
                    },
                    KeySchemaElement {
                        attributeName = "ShardId"
                        keyType = KeyType.Range
                    }
                )
                attributeDefinitions = listOf(
                    AttributeDefinition {
                        attributeName = "ApplicationName"
                        attributeType = ScalarAttributeType.S
                    },
                    AttributeDefinition {
                        attributeName = "ShardId"  
                        attributeType = ScalarAttributeType.S
                    }
                )
                billingMode = BillingMode.PayPerRequest
            }
            dynamoClient.createTable(request)
            
            // 테이블 생성 완료까지 대기
            delay(5000)
        }
    }
    
    /**
     * 체크포인트 정보를 DynamoDB에 저장
     *
     * @param checkpointData 저장할 체크포인트 데이터
     */
    suspend fun saveCheckpoint(checkpointData: CheckpointData) {
        try {
            val request = PutItemRequest {
                tableName = this@CheckpointManager.tableName
                item = mapOf(
                    "ApplicationName" to AttributeValue.S(applicationName),
                    "ShardId" to AttributeValue.S(checkpointData.shardId),
                    "SequenceNumber" to AttributeValue.S(checkpointData.sequenceNumber),
                    "SubSequenceNumber" to AttributeValue.N(checkpointData.subSequenceNumber.toString()),
                    "LastUpdateTime" to AttributeValue.N(checkpointData.lastUpdateTime.toString())
                )
            }
            dynamoClient.putItem(request)
            log.debug { "체크포인트 저장: ${checkpointData.shardId} -> ${checkpointData.sequenceNumber}" }
        } catch (e: Exception) {
            log.error(e) { "체크포인트 저장 실패: ${checkpointData.shardId}" }
        }
    }
    
    /**
     * 특정 샤드의 체크포인트 정보를 DynamoDB에서 조회
     *
     * @param shardId 조회할 샤드 ID
     * @return 체크포인트 데이터 또는 null (조회 실패 시)
     */
    suspend fun getCheckpoint(shardId: String): CheckpointData? {
        return try {
            val request = GetItemRequest {
                tableName = this@CheckpointManager.tableName
                key = mapOf(
                    "ApplicationName" to AttributeValue.S(applicationName),
                    "ShardId" to AttributeValue.S(shardId)
                )
            }
            val response = dynamoClient.getItem(request)
            
            response.item?.let { item ->
                CheckpointData(
                    shardId = item["ShardId"]?.asS() ?: shardId,
                    sequenceNumber = item["SequenceNumber"]?.asS() ?: "",
                    subSequenceNumber = item["SubSequenceNumber"]?.asN()?.toLongOrNull() ?: 0,
                    lastUpdateTime = item["LastUpdateTime"]?.asN()?.toLongOrNull() ?: 0
                )
            }
        } catch (e: Exception) {
            log.error(e) { "체크포인트 조회 실패: $shardId" }
            null
        }
    }
    
    /**
     * DynamoDB 클라이언트 연결 종료
     */
    suspend fun close() {
        dynamoClient.close()
    }
}