package net.kotlinx.aws.kinesis.reader

import aws.sdk.kotlin.services.kinesis.getRecords
import aws.sdk.kotlin.services.kinesis.model.GetShardIteratorRequest
import aws.sdk.kotlin.services.kinesis.model.ShardIteratorType
import aws.smithy.kotlin.runtime.time.fromEpochMilliseconds
import kotlinx.coroutines.CancellationException
import mu.KotlinLogging
import net.kotlinx.aws.kinesis.KinesisUtil
import net.kotlinx.aws.kinesis.kinesis
import net.kotlinx.concurrent.delay
import net.kotlinx.exception.toSimpleString
import net.kotlinx.string.abbr

/**
 * Kinesis 스트림의 특정 샤드에서 레코드를 읽고 처리하는 클래스
 * 샤드 1개당 1개의 포로세서가 작동한다
 */
class KinesisShardProcessor(private val reader: KinesisReader, private val shardId: String) {

    /** 로깅용 타이틀 */
    val title: String
        get() = "[${reader.streamName}/${reader.readerName}] -> #[$shardId]"

    /**
     * 샤드 처리 시작
     */
    suspend fun start() {

        log.info { "${title} 샤드 프로세서 시작" }

        try {
            var currentShardIterator = findInitShardIterator()

            while (currentShardIterator != null) {

                val resp = KinesisUtil.EXCEEDED_RETRY.withRetry {
                    reader.aws.kinesis.getRecords {
                        shardIterator = currentShardIterator
                        limit = reader.readChunkCnt
                    }
                }
                val records = resp.records!!

                if (records.isNotEmpty()) {
                    log.debug { " -> ${title} 샤드에서 레코드 수신 ${records.size}건" }
                    if (log.isTraceEnabled) {
                        records.forEach {
                            log.trace { "  ==> ${it.partitionKey} -> ${it.dataAsString.abbr(100)}" }
                        }
                    }
                    reader.recordHandler(shardId, records)

                    // 핸들러 처리가 정상적으로 끝나면 체크포인트 업데이트 (에러가 나면 업데이트 안함)
                    records.last().let { lastRecord ->
                        val kinesisCheckpointData = KinesisCheckpointData(
                            shardId = shardId,
                            sequenceNumber = lastRecord.sequenceNumber ?: "",
                            subSequenceNumber = 0
                        )
                        reader.checkpointManager.saveCheckpoint(kinesisCheckpointData)
                    }
                }

                currentShardIterator = resp.nextShardIterator
                if (currentShardIterator == null) {
                    //이경우 프로세서에 계속남아있지만, API상에서 샤드를 리턴해주기때문에 어쩔 수 없음.. 기다리면 사라짐
                    log.info { "${title} nextShardIterator 리턴이 null -> 마지막 레코드까지 읽었음 -> 샤드 중단" }
                }

                // 빈 응답인 경우 잠시 대기
                if (records.isEmpty()) {
                    log.trace { " ==> 빈레코드 리턴" }
                    reader.recordEmptyHandler(shardId)
                    reader.recordCheckInterval.delay()
                }
            }
        } catch (e: CancellationException) {
            log.info { "${title} 샤드 프로세서 중단요청 -> ${e.toSimpleString()}" }
        } catch (e: Exception) {
            log.error(e) { "${title} 샤드 프로세서 오류" }
        } finally {
            log.info { "${title} 샤드 프로세서 종료" }
        }
    }

    /**
     * 초기 샤드 이터레이터 가져오기
     * 체크포인트가 있으면 해당 시퀀스 번호 이후부터 시작하고, 없으면 최신부터 시작
     * @return shard가 closed 상태면 null 반환 가능
     */
    private suspend fun findInitShardIterator(): String? {
        val checkpoint = reader.checkpointManager.getCheckpoint(shardId)
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
            val request = GetShardIteratorRequest {
                streamName = reader.streamName
                shardId = this@KinesisShardProcessor.shardId
                if (reader.shardCheckpointDuration == null) {
                    shardIteratorType = ShardIteratorType.Latest
                } else {
                    shardIteratorType = ShardIteratorType.AtTimestamp  //Latest 하면 현시점부터 읽기 때문에, 샤드 갱신 오차만큼 더 읽어준다
                    timestamp = aws.smithy.kotlin.runtime.time.Instant.fromEpochMilliseconds(
                        System.currentTimeMillis() - reader.shardCheckpointDuration!!.inWholeMilliseconds
                    )
                }
            }
            log.info { "[${reader.streamName}/${reader.readerName}] -> 샤드 ${shardId} -> 체크포인트 없음! (레코드 수신시 새 체크포인트 생성) -> ${request.shardIteratorType}" }
            reader.aws.kinesis.getShardIterator(request).shardIterator
        }
    }


    companion object {
        private val log = KotlinLogging.logger {}
    }
}