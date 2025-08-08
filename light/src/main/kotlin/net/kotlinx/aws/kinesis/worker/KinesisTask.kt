package net.kotlinx.aws.kinesis.worker

import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.LazyAwsClientProperty
import net.kotlinx.aws.kinesis.reader.KinesisReader
import net.kotlinx.aws.kinesis.writer.KinesisWriteData
import net.kotlinx.aws.kinesis.writer.KinesisWriter
import net.kotlinx.core.Kdsl
import net.kotlinx.id.IdGenerator
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * 스트리밍 처리도구
 * Kinesis 스트림에 데이터를 넣고 응답을 기다리는 태스크 클래스
 *
 * 원하는 작업을 kinesis에 요청하면, 결과를 collect 해주는 도구
 * 실행전에, 워커가 정상 작동중인지 확인해주세요
 *
 * 아래의 요구사항을 따른다
 * 1. 고속 / 병렬 처리가 저렴하게 가능해야함 (샤드1개 월 1.3만원으로 초당 1000개 처리)
 * 2. 수평 확장/축소 가능 (런타임에 샤드 수 조정 가능)
 * 3. 대용량 데이터 처리 가능 (청크단위 요청/응답 처리)
 * 4. 실시간에 가까운(1초 이내도 가능) 반응
 * 5. 요청 / 응답을 flow로 간단하게 사용할 수 있어야함
 * 6. timeout 기능이 있어야 함
 *
 * 주의!
 * 1. 오류 처리시 중산시점부터 재시도하는 기능은 없음 -> collector  를 csv로 만들어서 셀프 구현할것
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

    /**
     * 태스크 이름
     * ex) xxJob
     *  */
    lateinit var taskName: String

    /** 체크포인트 테이블 이름 */
    lateinit var checkpointTableName: String

    /** 레코드 체크하는 주기 */
    var recordCheckInterval: Duration = 5.seconds

    /**
     * 샤드 옵션
     * 작업 요청자는 모든 샤드를 읽어야함
     *  */
    var shardOption: KinesisReader.KinesisReaderShardOption.KinesisReaderShardAll = KinesisReader.KinesisReaderShardOption.KinesisReaderShardAll()

    /** 한번에 읽어올 수 있는 최대 레코드 수 */
    var readChunkCnt: Int = 10000

    /** 최대 재시도 횟수 */
    var maxRetries: Int = 10

    /** 재시도 간 지연 시간  */
    var writeRetryDelay: Duration = 1.seconds

    /** 체크포인트 보존시간. task의 경우 짧게 지정해도됨 */
    var checkpointTtl: Duration = 10.days

    /** 태스크 실행 타임아웃. 이 시간이 지나면 태스크가 강제 종료됨 */
    var timeout: Duration = 1.hours

    /**
     * ID만 추출하게 해주면됨
     * 내부에서 다시 조함함
     *  */
    var toId: (GsonData) -> String = { it["id"].str!! }

    //==================================================== 내부 상태 ======================================================

    /** ID 생성기 */
    private val idGenerator by koinLazy<IdGenerator>()

    private val writer by lazy {
        KinesisWriter {
            aws = this@KinesisTask.aws
            streamName = this@KinesisTask.streamName
            maxRetries = this@KinesisTask.maxRetries
            writeRetryDelay = this@KinesisTask.writeRetryDelay
        }
    }

    //==================================================== 기능 ======================================================

    /**
     * 태스크 실행
     * 1. 요청 데이터 입력
     * 2. 리더 활성화 (체크포인트 없음)
     * 3. 응답 데이터를 Flow로 반환
     */
    suspend fun execute(inputFlow: Flow<List<GsonData>>): Flow<List<GsonData>> {

        val taskId = idGenerator.nextvalAsString()
        //val totalCnt = inputFlow.fold(0) { acc, list -> acc + list.size } //미리 전체를 구함
        val uniqueKeys = inputFlow.flatMapMerge { datas -> flow { datas.map { emit(toId(it)) } } }.toSet()

        log.trace { "step01 - flow start 해서 수신 대기" }
        val taskFlow = KinesisTaskFlow(this, taskId, uniqueKeys)
        taskFlow.startup() //스타트업 먼저 함

        log.trace { "step02 - 요청 데이터 입력" }
        inputFlow.collect { datas ->
            val records = datas.map { data ->
                val recordId = toId(data)
                val recordKey = KinesisTaskRecordKey(taskName, taskId, recordId)
                KinesisWriteData(recordKey.inPartitionKey, data)
            }
            writer.putRecords(records)
        }
        log.info { "taskId ${taskId} -> ${uniqueKeys.size}개의 데이터 입력 완료 -> 결과데이터 수신대기.." }

        log.trace { "step03 - 채널을 Flow로 변환하여 반환" }
        return taskFlow.recordChannel.consumeAsFlow()
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}