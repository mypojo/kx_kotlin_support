package net.kotlinx.aws.sqs.worker

import kotlinx.coroutines.flow.*
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.LazyAwsClientProperty
import net.kotlinx.aws.sqs.sendBatch
import net.kotlinx.aws.sqs.sqs
import net.kotlinx.core.Kdsl
import net.kotlinx.id.IdGenerator
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * SQS 처리도구
 * SQS 큐에 데이터를 넣고 응답을 기다리는 태스크 클래스
 *
 * 원하는 작업을 SQS에 요청하면, 결과를 collect 해주는 도구
 * 실행전에, 워커가 정상 작동중인지 확인해주세요
 *
 * 아래의 요구사항을 따른다
 * 1. 요청 / 응답을 flow로 간단하게 사용할 수 있어야함
 * 2. timeout 기능이 있어야 함
 */
class SqsTask {

    @Kdsl
    constructor(block: SqsTask.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정 ======================================================

    /** aws 클라이언트 */
    var aws: AwsClient by LazyAwsClientProperty()

    /** 요청 큐 URL */
    lateinit var requestQueueUrl: String

    /** 결과 큐 URL */
    lateinit var resultQueueUrl: String

    /**
     * 태스크 이름
     * ex) xxJob
     *  */
    lateinit var taskName: String

    /** 레코드 체크하는 주기 */
    var recordCheckInterval: Duration = 5.seconds

    /** 한번에 읽어올 수 있는 최대 메시지 수 */
    var maxNumberOfMessages: Int = 10

    /** 가시성 타임아웃 (초) */
    var visibilityTimeout: Int = 30

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

    //==================================================== 기능 ======================================================

    /**
     * 태스크 실행
     * 1. 요청 데이터 입력
     * 2. 결과 큐 폴링 시작
     * 3. 응답 데이터를 Flow로 반환
     */
    suspend fun execute(inputFlow: Flow<List<GsonData>>): Flow<List<GsonData>> {

        val taskId = idGenerator.nextvalAsString()
        val uniqueKeys = inputFlow.flatMapMerge { datas -> flow { datas.forEach { emit(toId(it)) } } }.toSet()

        log.trace { "step01 - flow start 해서 수신 대기" }
        val taskFlow = SqsTaskFlow(this, taskId, uniqueKeys)
        taskFlow.startup() // 스타트업 먼저 함

        log.trace { "step02 - 요청 데이터 입력" }
        inputFlow.collect { datas ->
            val messages = datas.map { data ->
                val recordId = toId(data)
                val recordKey = SqsTaskRecordKey(taskName, taskId, recordId)
                recordKey.inMessageId to data.toString()
            }
            
            // 메시지 배치 전송
            val messageMap = messages.toMap()
            val messageList = messageMap.values.toList()
            val failedMessages = aws.sqs.sendBatch(requestQueueUrl, messageList)
            
            if (failedMessages.isNotEmpty()) {
                log.warn { "Failed to send ${failedMessages.size} messages to SQS after retries." }
            }
        }
        log.info { "taskId ${taskId} -> ${uniqueKeys.size}개의 데이터 입력 완료 -> 결과데이터 수신대기.." }

        log.trace { "step03 - 채널을 Flow로 변환하여 반환" }
        return taskFlow.recordChannel.consumeAsFlow()
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}