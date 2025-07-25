package net.kotlinx.aws.kinesis.reader

import aws.sdk.kotlin.services.kinesis.model.DescribeStreamRequest
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.LazyAwsClientProperty
import net.kotlinx.aws.kinesis.kinesis
import net.kotlinx.core.Kdsl
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Kinesis 스트림의 샤드를 관리하고 레코드를 처리하는 매니저 클래스
 * KCL(Kinesis Client Library)과 유사한 역할을 수행
 */
class KinesisReader {

    @Kdsl
    constructor(block: KinesisReader.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정 ======================================================

    /** aws 클라이언트 */
    var aws: AwsClient by LazyAwsClientProperty()

    /** 스트림 이름 */
    lateinit var streamName: String

    /** 애플리케이션 이름 (체크포인트 저장에 사용) */
    lateinit var applicationName: String

    /** 레코드 처리 핸들러 함수 */
    lateinit var recordHandler: suspend (KinesisRecord) -> Unit

    /** 체크포인트 테이블 이름 (기본값: kinesis-checkpoints) */
    var checkpointTableName: String = "kinesis-checkpoints"

    /** AWS 리전 (기본값: ap-northeast-2) */
    var region: String = "ap-northeast-2"

    //==================================================== 내부 상태 ======================================================

    private val checkpointManager by lazy { 
        CheckpointManager {
            this.tableName = checkpointTableName
            this.applicationName = applicationName
            this.region = region
        }
    }
    private val shardProcessors = ConcurrentHashMap<String, Job>()
    private val isRunning = AtomicBoolean(false)

    companion object {
        private val log = KotlinLogging.logger {}
    }

    //==================================================== 기능 ======================================================

    /**
     * 컨슈머 매니저 시작
     * 샤드 모니터링 및 프로세서 관리를 시작
     */
    suspend fun start() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn { "Consumer Manager가 이미 실행 중입니다" }
            return
        }

        log.info { "Kinesis Consumer Manager 시작 - 스트림: $streamName" }

        // 체크포인트 테이블 생성
        checkpointManager.createTableIfNotExists()

        // 샤드 모니터링 및 프로세서 관리
        val managerJob = CoroutineScope(Dispatchers.IO).launch {
            while (isRunning.get()) {
                try {
                    manageShardsAndProcessors()
                    delay(30000) // 30초마다 샤드 상태 확인
                } catch (e: Exception) {
                    log.error(e) { "샤드 관리 중 오류 발생" }
                    delay(10000)
                }
            }
        }

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(Thread {
            runBlocking {
                stop()
            }
        })

        managerJob.join()
    }

    /**
     * 컨슈머 매니저 중지
     * 모든 샤드 프로세서 중지 및 리소스 정리
     */
    suspend fun stop() {
        if (!isRunning.compareAndSet(true, false)) {
            return
        }

        log.info { "Kinesis Consumer Manager 종료 중..." }

        // 모든 샤드 프로세서 중지
        shardProcessors.values.forEach { job ->
            job.cancel()
        }

        shardProcessors.values.forEach { job ->
            job.join()
        }

        shardProcessors.clear()
        checkpointManager.close()

        log.info { "Kinesis Consumer Manager 종료 완료" }
    }

    //==================================================== 내부 구현 ======================================================

    /**
     * 샤드 상태 확인 및 프로세서 관리
     * 새로운 샤드에 대해 프로세서 시작, 더 이상 존재하지 않는 샤드의 프로세서 중지
     */
    private suspend fun manageShardsAndProcessors() {
        val currentShards = getCurrentShards()
        val runningShards = shardProcessors.keys.toSet()

        // 새로운 샤드에 대해 프로세서 시작
        val newShards = currentShards - runningShards
        newShards.forEach { shardId ->
            startShardProcessor(shardId)
        }

        // 더 이상 존재하지 않는 샤드의 프로세서 중지
        val closedShards = runningShards - currentShards
        closedShards.forEach { shardId ->
            stopShardProcessor(shardId)
        }

        log.info { "샤드 상태 - 전체: ${currentShards.size}, 실행중: ${shardProcessors.size}" }
    }

    /**
     * 현재 스트림의 샤드 목록 조회
     */
    private suspend fun getCurrentShards(): Set<String> {
        return try {
            val request = DescribeStreamRequest {
                streamName = this@KinesisReader.streamName
            }
            val response = aws.kinesis.describeStream(request)
            response.streamDescription?.shards?.map { it.shardId ?: "" }?.toSet() ?: emptySet()
        } catch (e: Exception) {
            log.error(e) { "샤드 목록 조회 실패" }
            emptySet()
        }
    }

    /**
     * 특정 샤드에 대한 프로세서 시작
     */
    private fun startShardProcessor(shardId: String) {
        val processor = ShardProcessor(streamName, shardId, checkpointManager, recordHandler, region)
        val job = CoroutineScope(Dispatchers.IO).launch {
            processor.start()
        }

        shardProcessors[shardId] = job
        log.info { "샤드 프로세서 시작: $shardId" }
    }

    /**
     * 특정 샤드에 대한 프로세서 중지
     */
    private suspend fun stopShardProcessor(shardId: String) {
        shardProcessors[shardId]?.let { job: Job ->
            job.cancel()
            job.join()
            shardProcessors.remove(shardId)
            log.info { "샤드 프로세서 중지: $shardId" }
        }
    }
}