package net.kotlinx.aws.kinesis.reader

import aws.sdk.kotlin.services.kinesis.describeStream
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
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


/**
 * Kinesis 스트림의 샤드를 관리하고 레코드를 처리하는 매니저 클래스
 * KCL(Kinesis Client Library)과 유사한 역할을 수행
 *
 * 체크포인트가 없는경우!! 리더가 작동하고 있는 상태에서 데이터를 입력해야 데이터를 읽기 가능하며, 이렇게 해야 새 체크포인트가 생긴다
 *
 * 주의! 동일한 스트림&리더이름으로 병렬 처리한다면 오작동하니 주의할것!! 반드시 유니크한 DDB 체크포인트로 작동해야한다
 *
 * 샤드 처리 방식:
 * 1. KinesisReaderShardAll (기본값): 모든 샤드를 처리
 * 2. KinesisReaderShardPartial: 서버별 샤드 분할 처리 - 해시링 방식으로 안정적인 분산 처리
 *    - 전체 서버 수와 현재 서버 인덱스를 기반으로 샤드를 균등 분배
 *    - 예: 샤드[a,b,c,d], 3개 서버 → 서버0:[a,d], 서버1:[b], 서버2:[c]
 *
 *
 * 샤딩시 주의!
 * 샤드를 늘리거나 줄이면, endingSequenceNumber 가 null이 아니 closing 샤드가 생길 수 잇음 -> 24시간동안 읽기 가능
 * 즉 샤드 1개 -> 2개로 늘리면, 샤드가 3개로 보이고 그중 1개는 closing 샤드임 (closing된건 자동으로 쓰기 불가능해짐)
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
    lateinit var recordHandler: KinesisReaderRecordHandler

    /**
     * 레코드 체크하는 주기
     * 1초를 주는경우 셀프 ProvisionedThroughputExceededException 가 발생할 수 있음
     *  */
    var recordCheckInterval: Duration = 4.seconds

    /**
     * 한번에 읽어올수.
     * 샤드당 10,000개 or 10mb 중 적은거로 결정됨
     *  */
    var readChunkCnt: Int = 10000

    /** 체크포인트 보존시간 */
    var checkpointTtl: Duration = 10.days

    /** 빈값 핸들러. 일반적으로는 사용되지 않음 */
    var recordEmptyHandler: KinesisReaderEmptyHandler = {}

    //==================================================== 샤드 옵션 ======================================================

    /** 샤드 처리 옵션 */
    var shardOption: KinesisReaderShardOption = KinesisReaderShardOption.KinesisReaderShardAll()

    /**
     * 샤드 체크포인트 없을때, 어디서부터 읽을지?
     * null이면 latest
     * 입력하면 현재시간 기준으로 해당 시간만큼 과거부터 읽어옴
     *  */
    var shardCheckpointDuration: Duration? = null

    sealed interface KinesisReaderShardOption {

        val checkInterval: Duration

        /**
         * 모든 샤드를 읽는 옵션
         * 샤드 체크 주기를 설정할 수 있음
         */
        data class KinesisReaderShardAll(override val checkInterval: Duration = 10.minutes) : KinesisReaderShardOption

        /**
         * 부분 샤드를 읽는 옵션
         * 전체 서버 수와 현재 서버의 인덱스를 받아 할당된 샤드만 처리
         */
        data class KinesisReaderShardPartial(
            /** 서버 전체 수 */
            val totalServerCount: Int,
            /** 0부터 시작함! */
            val serverIndex: Int,
            override val checkInterval: Duration = 10.minutes,
        ) : KinesisReaderShardOption {
            init {
                require(totalServerCount > 0) { "totalServerCount는 0보다 커야 합니다" }
                require(serverIndex >= 0) { "serverIndex는 0보다 크거나 같아야 합니다" }
                require(serverIndex < totalServerCount) { "serverIndex는 totalServerCount보다 작아야 합니다" }
            }
        }

    }


    //==================================================== 내부 상태 ======================================================

    internal val checkpointManager = KinesisCheckpointManager(this)

    private val shardProcessors = ConcurrentHashMap<String, Job>()
    private val isRunning = AtomicBoolean(false)

    //==================================================== 기능 ======================================================

    /** 로깅용 문구 */
    private val title: String get() = "[${streamName}/${readerName}]"

    /**
     * 컨슈머 매니저 시작
     * 샤드 모니터링 및 프로세서 관리를 시작
     */
    suspend fun start() {

        if (!isRunning.compareAndSet(false, true)) {
            log.warn { "[${readerName}]  -> 이미 실행 중입니다" }
            return
        }

        log.info { "${title} start.." }

        // 샤드 모니터링 및 프로세서 관리
        val managerJob = CoroutineScope(Dispatchers.IO).launch {
            while (isRunning.get()) {
                try {
                    manageShards()
                    shardOption.checkInterval.delay()
                } catch (e: Exception) {
                    log.error(e) { "샤드 관리 중 오류 발생" }
                    shardOption.checkInterval.delay()
                }
            }
        }

        // 셧다운 후크 달아주기
        Runtime.getRuntime().addShutdownHook(Thread { runBlocking { stop() } })

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

        log.debug { "${title} 종료 중..." }

        // 모든 샤드 프로세서 중지
        shardProcessors.values.forEach { it.cancel() }
        shardProcessors.values.joinAll()

        shardProcessors.clear()
        log.info { "${title} 종료 완료" }
    }

    //==================================================== 내부 구현 ======================================================

    /**
     * 샤드 할당 로직 - 해시링 방식으로 안정적인 샤드 분배
     * @param allShards 전체 샤드 목록
     * @return 현재 서버에 할당된 샤드 목록
     */
    private fun getAssignedShards(allShards: Set<String>): Set<String> {
        return when (val option = shardOption) {
            is KinesisReaderShardOption.KinesisReaderShardAll -> {
                allShards
            }

            is KinesisReaderShardOption.KinesisReaderShardPartial -> {
                // 샤드를 정렬하여 일관된 순서 보장
                val sortedShards = allShards.sorted()
                val assignedShards = mutableSetOf<String>()

                // 해시링 방식으로 샤드 분배
                sortedShards.forEachIndexed { index, shardId ->
                    if (index % option.totalServerCount == option.serverIndex) {
                        assignedShards.add(shardId)
                    }
                }

                assignedShards
            }
        }
    }

    /**
     * 샤드 상태 확인 및 프로세서 관리
     * 새로운 샤드에 대해 프로세서 시작, 더 이상 존재하지 않는 샤드의 프로세서 중지
     */
    private suspend fun manageShards() {

        /** 현재 스트림의 전체 샤드 목록 */
        val allShards = aws.kinesis.describeStream { streamName = this@KinesisReader.streamName }
            .streamDescription?.shards!!.let { shards ->
                val closingShards = shards.filter { it.sequenceNumberRange?.endingSequenceNumber != null }
                if (closingShards.isNotEmpty()) {

                    log.warn { " -> 전체 ${shards.size}개중 ${closingShards.size}개의 샤드가 닫혀있습니다. (닫은후 24시간 동안 읽을 수 있습니다) -> ${closingShards.map { it.shardId }}" }
                }

                //닫힌 서버를 맨 뒤로 빼준다
                shards.sortedBy { it.sequenceNumberRange?.endingSequenceNumber != null }.map { it.shardId }.toSet()
            }

        /** 현재 서버에 할당된 샤드만 필터링 */
        val assignedShards = getAssignedShards(allShards)

        val runningShards = shardProcessors.keys.toSet()

        // 새로운 샤드에 대해 프로세서 시작
        val newShards = assignedShards - runningShards
        newShards.forEach { shardId ->
            val processor = KinesisShardProcessor(this, shardId)
            val job = CoroutineScope(Dispatchers.IO).launch {
                processor.start()
            }
            shardProcessors[shardId] = job
        }

        // 더 이상 할당되지 않은 샤드의 프로세서 중지
        val closedShards = runningShards - assignedShards
        closedShards.forEach { shardId ->
            shardProcessors[shardId]?.let { job: Job ->
                job.cancel()
                job.join()
                shardProcessors.remove(shardId)
            }
        }

        log.info {
            val shardInfo = when (val op = shardOption) {
                is KinesisReaderShardOption.KinesisReaderShardAll ->
                    "전체 샤드 모두 할당됨 -> ${assignedShards.size}개"

                is KinesisReaderShardOption.KinesisReaderShardPartial ->
                    "전체서버 ${op.totalServerCount}대중 ${op.serverIndex + 1}번 서버 -> 전체샤드 ${allShards.size}개중 ${assignedShards.size}건 할당 -> ${assignedShards}"
            }
            " => ${title} 주기적인 샤드 관리완료 -> $shardInfo & 기존 실행중 ${runningShards.size}개"
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}