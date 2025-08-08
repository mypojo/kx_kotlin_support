package net.kotlinx.aws.kinesis.worker

import aws.sdk.kotlin.services.kinesis.model.Record
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.LazyAwsClientProperty
import net.kotlinx.aws.kinesis.reader.KinesisReader
import net.kotlinx.aws.kinesis.writer.KinesisWriteData
import net.kotlinx.aws.kinesis.writer.KinesisWriter
import net.kotlinx.core.Kdsl
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Kinesis 스트림의 특정 파티션 데이터를 읽어서 다른 파티션으로 전송하는 워커 클래스
 *
 * 파티션 키 규칙: taskName-taskId-type
 * - 요청: taskName-taskId-in
 * - 응답: taskName-taskId-out
 *
 * 워커는 "in" 타입의 레코드를 읽고 처리한 후, "out" 타입으로 변환하여 다시 입력
 */
class KinesisWorker {

    @Kdsl
    constructor(block: KinesisWorker.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정 ======================================================

    /** aws 클라이언트 */
    var aws: AwsClient by LazyAwsClientProperty()

    /** 스트림 이름 */
    lateinit var streamName: String

    /** 애플리케이션 이름 (체크포인트 저장에 사용) */
    var readerName: String = "worker01"

    /** 체크포인트 테이블 이름 */
    lateinit var checkpointTableName: String

    /** 레코드 체크하는 주기 */
    var recordCheckInterval: Duration = 3.seconds

    /**
     * 샤드 옵션
     * 워커가 여러대 있을경우 수정할것!
     *  */
    var shardOption: KinesisReader.KinesisReaderShardOption = KinesisReader.KinesisReaderShardOption.KinesisReaderShardAll()

    /**
     * 한번에 읽어올수.
     * 샤드당 10,000개 or 10mb 중 적은거로 결정됨
     *  */
    var readChunkCnt: Int = 10000

    /** 최대 재시도 횟수 */
    var maxRetries: Int = 10

    /** 재시도 간 지연 시간*/
    var writeRetryDelay: Duration = 1.seconds

    /** 실제 작업을 처리하는 핸들러 */
    lateinit var handler: suspend (List<KinesisTaskRecord>) -> Unit

    /** 종료되었을경우 콜백 알람등의 처리 */
    var stopCallback: suspend (KinesisWorker) -> Unit = {}

    //==================================================== 내부 상태 ======================================================

    private val reader by lazy {
        KinesisReader {
            aws = this@KinesisWorker.aws
            streamName = this@KinesisWorker.streamName
            readerName = this@KinesisWorker.readerName
            checkpointTableName = this@KinesisWorker.checkpointTableName
            recordCheckInterval = this@KinesisWorker.recordCheckInterval
            shardOption = this@KinesisWorker.shardOption
            readChunkCnt = this@KinesisWorker.readChunkCnt
            recordHandler = this@KinesisWorker::handleRecords
        }
    }

    private val writer by lazy {
        KinesisWriter {
            aws = this@KinesisWorker.aws
            streamName = this@KinesisWorker.streamName
            maxRetries = this@KinesisWorker.maxRetries
            writeRetryDelay = this@KinesisWorker.writeRetryDelay
        }
    }

    //==================================================== 기능 ======================================================

    /**
     * 워커 시작
     * Kinesis 스트림에서 데이터 읽기 시작
     */
    suspend fun start() {
        log.info { "KinesisWorker 시작 - 스트림: $streamName, 리더: $readerName" }
        reader.start()
    }

    /**
     * 워커 중지
     * intellij 에서 JVM 강제 중단해도 호출된다
     */
    suspend fun stop() {
        stopCallback(this)
        log.info { "KinesisWorker 종료 중..." }
        reader.stop()
        log.info { "KinesisWorker 종료 완료" }
    }

    /**
     * 레코드 처리 핸들러
     * "in" 타입 데이터만 필터링하여 처리 후 "out" 타입으로 전송
     */
    private suspend fun handleRecords(shardId: String, records: List<Record>) {
        // "in" 타입 데이터만 필터링
        val filteredRecords = records.filter { KinesisTaskRecordKey.isIn(it.partitionKey!!) }

        if (filteredRecords.isEmpty()) {
            log.debug { " -> #[$shardId] ${filteredRecords.size}건의 스트림 읽었으나 -> 현재 워커에서 처리할 레코드 없음 (타입: in)" }
            return
        }

        log.info { " -> #[$shardId] ${filteredRecords.size}개의 레코드 처리 시작 (타입: in)" }

        val records = filteredRecords.map { KinesisTaskRecord(it) }
        handler.invoke(records)

        val returnRecords = records.map { KinesisWriteData(it.partitonKey.outPartitionKey, it.result) }
        writer.putRecords(returnRecords)
        log.info { " -> #[$shardId] ${filteredRecords.size}개의 레코드 처리 완료 & 결과 push 완료" }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}

