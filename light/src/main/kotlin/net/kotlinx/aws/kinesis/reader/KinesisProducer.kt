package net.kotlinx.aws.kinesis.reader

import aws.sdk.kotlin.services.kinesis.KinesisClient
import aws.sdk.kotlin.services.kinesis.model.PutRecordRequest
import aws.sdk.kotlin.services.kinesis.model.PutRecordsRequest
import aws.sdk.kotlin.services.kinesis.model.PutRecordsRequestEntry
import mu.KotlinLogging

/**
 * Kinesis 스트림에 레코드를 전송하는 프로듀서 클래스
 *
 * @property region AWS 리전 (기본값: ap-northeast-2)
 */
class KinesisProducer(
    private val region: String = "ap-northeast-2"
) {
    private val kinesisClient = KinesisClient { 
        this.region = this@KinesisProducer.region 
    }
    
    companion object {
        private val log = KotlinLogging.logger {}
    }
    
    /**
     * 단일 레코드를 Kinesis 스트림에 전송
     *
     * @param streamName 스트림 이름
     * @param data 전송할 데이터 (문자열)
     * @param partitionKey 파티션 키
     * @return 시퀀스 번호 또는 null (전송 실패 시)
     */
    suspend fun putRecord(streamName: String, data: String, partitionKey: String): String? {
        return try {
            val request = PutRecordRequest {
                this.streamName = streamName
                this.data = data.toByteArray()
                this.partitionKey = partitionKey
            }
            
            val response = kinesisClient.putRecord(request)
            log.info { "레코드 전송 완료 - ShardId: ${response.shardId}, SequenceNumber: ${response.sequenceNumber}" }
            response.sequenceNumber
        } catch (e: Exception) {
            log.error(e) { "레코드 전송 실패: ${e.message}" }
            null
        }
    }
    
    /**
     * 여러 레코드를 Kinesis 스트림에 배치로 전송
     *
     * @param streamName 스트림 이름
     * @param records 전송할 레코드 목록 (데이터, 파티션 키) 쌍의 리스트
     * @return 성공적으로 전송된 레코드 수
     */
    suspend fun putRecords(streamName: String, records: List<Pair<String, String>>): Int {
        return try {
            val entries = records.map { (data, partitionKey) ->
                PutRecordsRequestEntry {
                    this.data = data.toByteArray()
                    this.partitionKey = partitionKey
                }
            }
            
            val request = PutRecordsRequest {
                this.streamName = streamName
                this.records = entries
            }
            
            val response = kinesisClient.putRecords(request)
            val successCount = response.records?.count { it.errorCode == null } ?: 0
            log.info { "배치 레코드 전송 완료 - 성공: ${successCount}개" }
            successCount
        } catch (e: Exception) {
            log.error(e) { "배치 레코드 전송 실패: ${e.message}" }
            0
        }
    }
    
    /**
     * 클라이언트 연결 종료
     */
    suspend fun close() {
        kinesisClient.close()
    }
}