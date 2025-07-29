package net.kotlinx.aws.kinesis.worker

import aws.sdk.kotlin.services.kinesis.model.Record
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.LazyAwsClientProperty
import net.kotlinx.aws.kinesis.reader.KinesisReader
import net.kotlinx.aws.kinesis.writer.KinesisWriter
import net.kotlinx.core.Kdsl
import net.kotlinx.json.gson.GsonData
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Kinesis 스트림에 데이터를 넣고 응답을 기다리는 태스크 클래스
 *
 * 파티션 키 규칙: taskName-taskId-type
 * - 요청: taskName-taskId-in
 * - 응답: taskName-taskId-out
 */
class KinesisTask {

    @Kdsl
    constructor(block: KinesisTask.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정 ======================================================

    /** aws 클라이언트 */
    var aws: AwsClient by LazyAwsClientProperty()

    /** 스트림 이름 */
    lateinit var streamName: String

    /** 태스크 이름 */
    lateinit var taskName: String

    /** 태스크 ID */
    lateinit var taskId: String

    /** 체크포인트 테이블 이름 */
    lateinit var checkpointTableName: String

    /** 레코드 체크하는 주기 */
    var recordCheckInterval: Duration = 1.seconds

    /** 샤드 체크하는 주기 */
    var shardCheckInterval: Duration = 60.seconds

    /** 한번에 읽어올 수 있는 최대 레코드 수 */
    var readChunkCnt: Int = 10000

    /** 최대 재시도 횟수 */
    var maxRetries: Int = 10

    /** 재시도 간 지연 시간 (기본값: 1초) */
    var retryDelay: Duration = 1.seconds


    //==================================================== 내부 상태 ======================================================

    /** 요청 파티션 키 (taskName-taskId-in) */
    private val inPartitionKey: String
        get() = "$taskName-$taskId-in"

    /** 응답 파티션 키 (taskName-taskId-out) */
    private val outPartitionKey: String
        get() = "$taskName-$taskId-out"

    /** 리더 이름 (task-taskId) */
    private val taskReaderName: String
        get() = "task-$taskId"

    private val writer by lazy {
        KinesisWriter {
            aws = this@KinesisTask.aws
            streamName = this@KinesisTask.streamName
            partitionKeyBuilder = { this@KinesisTask.inPartitionKey }
            maxRetries = this@KinesisTask.maxRetries
            retryDelay = this@KinesisTask.retryDelay
        }
    }

    private val receivedCount = AtomicInteger(0)
    private var isCompleted = false

    //==================================================== 기능 ======================================================

    /**
     * 태스크 실행
     * 1. 요청 데이터 입력
     * 2. 리더 활성화 (체크포인트 없음)
     * 3. 응답 데이터 대기
     */
    suspend fun execute(inputFlow: Flow<List<GsonData>>): Int {
        // 1. 요청 데이터 입력
        val totalCnt = inputFlow.map {
            writer.putRecords(it)
            it.size
        }.toList().sum()

        log.info { "요청 파티션($inPartitionKey)에 ${totalCnt}개의 데이터 입력 완료" }

        // 2. 리더 활성화 (체크포인트 없음)
        log.info { "리더 활성화 중 (readerName: $taskReaderName)..." }
        val (reader, readerJob) = activateReader(totalCnt)

        log.info { "응답 대기 중..." }
        // 완료될 때까지 대기
        while (!isCompleted) {
            delay(1000)
        }

        // 리더 종료
        stopReader(reader, readerJob)
        return receivedCount.get()
    }

    /**
     * 리더 활성화
     * 체크포인트가 없는 상태에서 리더를 시작하여 새로운 체크포인트 생성
     * @param dataCount 처리할 데이터 수
     * @param onComplete 태스크 완료 후 호출될 콜백 함수
     * @return Pair<KinesisReader, Job> 생성된 리더와 리더 작업
     */
    private suspend fun activateReader(dataCount: Int): Pair<KinesisReader, Job> {
        val reader = KinesisReader {
            aws = this@KinesisTask.aws
            streamName = this@KinesisTask.streamName
            readerName = this@KinesisTask.taskReaderName
            checkpointTableName = this@KinesisTask.checkpointTableName
            recordCheckInterval = this@KinesisTask.recordCheckInterval
            shardCheckInterval = this@KinesisTask.shardCheckInterval
            readChunkCnt = this@KinesisTask.readChunkCnt
            recordHandler = { shardId, records ->
                handleRecords(shardId, records, dataCount)
            }
        }

        val readerJob = CoroutineScope(Dispatchers.IO).launch {
            reader.start()
        }

        // 리더가 초기화될 시간을 주기 위해 잠시 대기
        delay(2000)

        return Pair(reader, readerJob)
    }

    /**
     * 리더 종료
     * @param reader 종료할 리더
     * @param readerJob 종료할 리더 작업
     */
    private suspend fun stopReader(reader: KinesisReader, readerJob: Job) {
        log.info { "KinesisTask 종료 중..." }
        reader.stop()
        readerJob.cancelAndJoin()
        log.info { "KinesisTask 종료 완료" }
    }

    /**
     * 레코드 처리 핸들러
     * out 타입 파티션 데이터만 필터링하여 카운트
     * @param shardId 샤드 ID
     * @param records 처리할 레코드 목록
     * @param dataCount 처리할 데이터 수
     */
    private fun handleRecords(shardId: String, records: List<Record>, dataCount: Int) {
        // out 파티션 데이터만 필터링
        val filteredRecords = records.filter { it.partitionKey == outPartitionKey }

        if (filteredRecords.isEmpty()) {
            log.debug { "[$shardId] 처리할 레코드 없음 (대상 파티션: $outPartitionKey)" }
            return
        }

        log.info { "[$shardId] ${filteredRecords.size}개의 레코드 처리 시작 (대상 파티션: $outPartitionKey)" }

        // 레코드 카운트 증가
        val newCount = receivedCount.addAndGet(filteredRecords.size)
        log.info { "[$shardId] 현재까지 받은 레코드 수: $newCount / $dataCount" }

        // 모든 응답을 받았는지 확인
        if (newCount >= dataCount && !isCompleted) {
            isCompleted = true
            log.info { "[$shardId] 모든 응답($dataCount 개)을 받았습니다. 태스크 완료 처리 중..." }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}