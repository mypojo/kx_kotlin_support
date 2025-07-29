package net.kotlinx.aws.kinesis.reader

import aws.sdk.kotlin.services.kinesis.model.DescribeStreamRequest
import aws.sdk.kotlin.services.kinesis.model.Record
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.LazyAwsClientProperty
import net.kotlinx.aws.kinesis.kinesis
import net.kotlinx.concurrent.delay
import net.kotlinx.core.Kdsl
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Kinesis 스트림의 샤드를 관리하고 레코드를 처리하는 매니저 클래스
 * KCL(Kinesis Client Library)과 유사한 역할을 수행
 *
 * 체크포인트가 없는경우!! 리더가 작동하고 있는 상태에서 데이터를 입력해야 데이터를 읽기 가능하며, 이렇게 해야 새 체크포인트가 생긴다
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
    lateinit var readerName: String

    /**
     * 체크포인트 테이블 이름
     * ex) system-dev
     *  */
    lateinit var checkpointTableName: String

    /** 레코드 처리 핸들러 함수 */
    lateinit var recordHandler: suspend (String, List<Record>) -> Unit

    /** 레코드 체크하는 주기 */
    var recordCheckInterval: Duration = 1.seconds

    /** 사드 체크하는 주기 */
    var shardCheckInterval: Duration = 1.minutes

    /**
     * 한번에 읽어올수.
     * 샤드당 10,000개 or 10mb 중 적은거로 결정됨
     *  */
    var readChunkCnt: Int = 10000

    //==================================================== 내부 상태 ======================================================

    internal val kinesisCheckpointManager = KinesisCheckpointManager(this)

    private val shardProcessors = ConcurrentHashMap<String, Job>()
    private val isRunning = AtomicBoolean(false)

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

        // 샤드 모니터링 및 프로세서 관리
        val managerJob = CoroutineScope(Dispatchers.IO).launch {
            while (isRunning.get()) {
                try {
                    manageShardsAndProcessors()
                    shardCheckInterval.delay()
                } catch (e: Exception) {
                    log.error(e) { "샤드 관리 중 오류 발생" }
                    shardCheckInterval.delay()
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
        shardProcessors.values.forEach { it.cancel() }
        shardProcessors.values.forEach { it.join() }

        shardProcessors.clear()
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
        log.info { "kinesis [$streamName] 샤드 관리완료 -> 현재샤드 ${currentShards.size} / 작동중인샤드 ${runningShards.size}" }
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
            throw e
        }
    }

    /**
     * 특정 샤드에 대한 프로세서 시작
     */
    private fun startShardProcessor(shardId: String) {
        val processor = KinesisShardProcessor(this, shardId)
        val job = CoroutineScope(Dispatchers.IO).launch {
            processor.start()
        }
        shardProcessors[shardId] = job
    }

    /**
     * 특정 샤드에 대한 프로세서 중지
     */
    private suspend fun stopShardProcessor(shardId: String) {
        shardProcessors[shardId]?.let { job: Job ->
            job.cancel()
            job.join()
            shardProcessors.remove(shardId)
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}