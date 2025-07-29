package net.kotlinx.aws.kinesis.reader

import aws.sdk.kotlin.services.kinesis.getRecords
import aws.sdk.kotlin.services.kinesis.model.GetShardIteratorRequest
import aws.sdk.kotlin.services.kinesis.model.ShardIteratorType
import mu.KotlinLogging
import net.kotlinx.aws.kinesis.kinesis
import net.kotlinx.concurrent.delay
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Kinesis 스트림의 특정 샤드에서 레코드를 읽고 처리하는 클래스
 */
class KinesisShardProcessor(private val reader: KinesisReader, private val shardId: String) {

    private val isRunning = AtomicBoolean(false)

    /**
     * 샤드 처리 시작
     */
    suspend fun start() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn { "샤드 프로세서가 이미 실행 중입니다: $shardId" }
            return
        }

        log.info { "[$shardId] 샤드 프로세서 시작" }

        try {
            var currentShardIterator = findInitShardIterator()

            while (isRunning.get() && currentShardIterator != null) {

                val resp = reader.aws.kinesis.getRecords {
                    shardIterator = currentShardIterator
                    limit = reader.readChunkCnt
                }
                val records = resp.records!!

                if (records.isNotEmpty()) {

                    log.debug { " ->  [$shardId] 레코드 처리 시작 -> ${records.size}건" }
                    reader.recordHandler(shardId, records)

                    // 핸들러 처리가 정상적으로 끝나면 체크포인트 업데이트 (에러가 나면 업데이트 안함)
                    val lastRecord = records.last()
                    val kinesisCheckpointData = KinesisCheckpointData(
                        shardId = shardId,
                        sequenceNumber = lastRecord.sequenceNumber ?: "",
                        subSequenceNumber = 0
                    )
                    reader.kinesisCheckpointManager.saveCheckpoint(kinesisCheckpointData)
                }

                currentShardIterator = resp.nextShardIterator

                // 빈 응답인 경우 잠시 대기
                if (records.isEmpty()) {
                    log.trace { " ==> 빈레코드 리턴" }
                    reader.recordCheckInterval.delay()
                }
            }
        } catch (e: Exception) {
            log.error(e) { "[$shardId] 샤드 프로세서 오류" }
        } finally {
            isRunning.set(false)
            reader.aws.kinesis.close()
            log.info { "[$shardId] 샤드 프로세서 종료" }
        }
    }

    /**
     * 초기 샤드 이터레이터 가져오기
     * 체크포인트가 있으면 해당 시퀀스 번호 이후부터 시작하고, 없으면 최신부터 시작
     * @return shard가 closed 상태면 null 반환 가능
     */
    private suspend fun findInitShardIterator(): String? {
        val checkpoint = reader.kinesisCheckpointManager.getCheckpoint(shardId)
        return if (checkpoint != null) {
            // 체크포인트가 있으면 해당 시퀀스 번호 이후부터 시작
            val request = GetShardIteratorRequest {
                streamName = reader.streamName
                shardId = this@KinesisShardProcessor.shardId
                shardIteratorType = ShardIteratorType.AfterSequenceNumber
                startingSequenceNumber = checkpoint.sequenceNumber
            }
            reader.aws.kinesis.getShardIterator(request).shardIterator
        } else {
            // 체크포인트가 없으면 최신부터 시작
            log.warn { "체크포인트가 없음!! 리더 가동중에 새 데이터가 입력되어야 체크포인트가 생기고, 이후 데이터를 누락없이 받을 수 있음!!" }
            val request = GetShardIteratorRequest {
                streamName = reader.streamName
                shardId = this@KinesisShardProcessor.shardId
                shardIteratorType = ShardIteratorType.Latest
            }
            reader.aws.kinesis.getShardIterator(request).shardIterator
        }
    }


    /**
     * 샤드 처리 중지
     */
    fun stop() {
        isRunning.set(false)
    }


    companion object {
        private val log = KotlinLogging.logger {}
    }
}