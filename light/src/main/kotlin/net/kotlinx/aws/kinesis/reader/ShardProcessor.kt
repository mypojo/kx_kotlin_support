package net.kotlinx.aws.kinesis.reader

import aws.sdk.kotlin.services.kinesis.KinesisClient
import aws.sdk.kotlin.services.kinesis.model.GetRecordsRequest
import aws.sdk.kotlin.services.kinesis.model.GetShardIteratorRequest
import aws.sdk.kotlin.services.kinesis.model.ShardIteratorType
import kotlinx.coroutines.delay
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Kinesis 스트림의 특정 샤드에서 레코드를 읽고 처리하는 클래스
 *
 * @property streamName Kinesis 스트림 이름
 * @property shardId 처리할 샤드 ID
 * @property checkpointManager 체크포인트 관리자
 * @property recordHandler 레코드 처리 핸들러 함수
 * @property region AWS 리전 (기본값: ap-northeast-2)
 */
class ShardProcessor(
    private val streamName: String,
    private val shardId: String,
    private val checkpointManager: CheckpointManager,
    private val recordHandler: suspend (KinesisRecord) -> Unit,
    private val region: String = "ap-northeast-2"
) {
    private val kinesisClient = KinesisClient { 
        this.region = this@ShardProcessor.region 
    }
    private val isRunning = AtomicBoolean(false)
    
    companion object {
        private val log = KotlinLogging.logger {}
    }
    
    /**
     * 샤드 처리 시작
     */
    suspend fun start() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn { "샤드 프로세서가 이미 실행 중입니다: $shardId" }
            return
        }
        
        log.info { "샤드 프로세서 시작: $shardId" }
        
        try {
            var shardIterator = getInitialShardIterator()
            
            while (isRunning.get() && shardIterator != null) {
                val request = GetRecordsRequest {
                    this.shardIterator = shardIterator
                    limit = 100 // 한 번에 최대 100개 레코드
                }
                val records = kinesisClient.getRecords(request)
                
                if (records.records?.isNotEmpty() == true) {
                    processRecords(records.records!!)
                    
                    // 마지막 레코드로 체크포인트 업데이트
                    val lastRecord = records.records!!.last()
                    val checkpointData = CheckpointData(
                        shardId = shardId,
                        sequenceNumber = lastRecord.sequenceNumber ?: "",
                        subSequenceNumber = 0
                    )
                    checkpointManager.saveCheckpoint(checkpointData)
                }
                
                shardIterator = records.nextShardIterator
                
                // 빈 응답인 경우 잠시 대기
                if (records.records?.isEmpty() != false) {
                    delay(1000)
                }
            }
        } catch (e: Exception) {
            log.error(e) { "샤드 프로세서 오류: $shardId" }
        } finally {
            isRunning.set(false)
            kinesisClient.close()
            log.info { "샤드 프로세서 종료: $shardId" }
        }
    }
    
    /**
     * 초기 샤드 이터레이터 가져오기
     * 체크포인트가 있으면 해당 시퀀스 번호 이후부터 시작하고, 없으면 최신부터 시작
     */
    private suspend fun getInitialShardIterator(): String? {
        val checkpoint = checkpointManager.getCheckpoint(shardId)
        
        return if (checkpoint != null && checkpoint.sequenceNumber.isNotEmpty()) {
            // 체크포인트가 있으면 해당 시퀀스 번호 이후부터 시작
            val request = GetShardIteratorRequest {
                streamName = this@ShardProcessor.streamName
                shardId = this@ShardProcessor.shardId
                shardIteratorType = ShardIteratorType.AfterSequenceNumber
                startingSequenceNumber = checkpoint.sequenceNumber
            }
            kinesisClient.getShardIterator(request).shardIterator
        } else {
            // 체크포인트가 없으면 최신부터 시작
            val request = GetShardIteratorRequest {
                streamName = this@ShardProcessor.streamName
                shardId = this@ShardProcessor.shardId
                shardIteratorType = ShardIteratorType.Latest
            }
            kinesisClient.getShardIterator(request).shardIterator
        }
    }
    
    /**
     * 레코드 처리
     */
    private suspend fun processRecords(records: List<aws.sdk.kotlin.services.kinesis.model.Record>) {
        log.info { "레코드 처리 시작 - 샤드: $shardId, 개수: ${records.size}" }
        
        records.forEach { record ->
            try {
                val kinesisRecord = KinesisRecord(
                    sequenceNumber = record.sequenceNumber ?: "",
                    partitionKey = record.partitionKey ?: "",
                    data = record.data?.decodeToString() ?: "",
                    approximateArrivalTimestamp = record.approximateArrivalTimestamp?.epochSeconds ?: 0,
                    shardId = shardId
                )
                
                recordHandler(kinesisRecord)
            } catch (e: Exception) {
                log.error(e) { "레코드 처리 실패 - 시퀀스: ${record.sequenceNumber}" }
            }
        }
    }
    
    /**
     * 샤드 처리 중지
     */
    fun stop() {
        isRunning.set(false)
    }
}