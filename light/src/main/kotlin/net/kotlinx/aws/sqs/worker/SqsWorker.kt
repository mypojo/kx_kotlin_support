package net.kotlinx.aws.sqs.worker

import aws.sdk.kotlin.services.sqs.model.Message
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.LazyAwsClientProperty
import net.kotlinx.aws.sqs.deleteMessageBatch
import net.kotlinx.aws.sqs.receiveMessage
import net.kotlinx.aws.sqs.sendBatch
import net.kotlinx.aws.sqs.sqs
import net.kotlinx.concurrent.delay
import net.kotlinx.core.Kdsl
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * SQS 큐의 메시지를 읽어서 다른 큐로 전송하는 워커 클래스
 *
 * 메시지 ID 규칙: taskName-taskId-type
 * - 요청: taskName-taskId-in
 * - 응답: taskName-taskId-out
 *
 * 워커는 "in" 타입의 메시지를 읽고 처리한 후, "out" 타입으로 변환하여 다시 입력
 */
class SqsWorker {

    @Kdsl
    constructor(block: SqsWorker.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정 ======================================================

    /** aws 클라이언트 */
    var aws: AwsClient by LazyAwsClientProperty()

    /** 요청 큐 URL */
    lateinit var requestQueueUrl: String

    /** 결과 큐 URL */
    lateinit var resultQueueUrl: String

    /** 워커 이름 */
    var workerName: String = "worker01"

    /** 레코드 체크하는 주기. 롱폴링(20s)을 사용함으로 짧게 줘도 됨 */
    var recordCheckInterval: Duration = 5.seconds

    /** 한번에 읽어올 수 있는 최대 메시지 수 */
    var maxNumberOfMessages: Int = 10

    /** 가시성 타임아웃 (초) */
    var visibilityTimeout: Int = 30

    /** 실제 작업을 처리하는 핸들러 */
    lateinit var handler: suspend (List<SqsTaskRecord>) -> Unit

    /** 종료되었을경우 콜백 알람등의 처리 */
    var stopCallback: suspend (SqsWorker) -> Unit = {}

    /** 실행 여부 */
    private var isRunning = false

    //==================================================== 기능 ======================================================

    /**
     * 워커 시작
     * SQS 큐에서 데이터 읽기 시작
     */
    suspend fun start() {
        log.info { "SqsWorker 시작 - 요청 큐: $requestQueueUrl, 결과 큐: $resultQueueUrl, 워커: $workerName" }
        isRunning = true

        try {
            while (isRunning) {
                // 메시지 수신
                val messages = aws.sqs.receiveMessage(requestQueueUrl, visibilityTimeout, maxNumberOfMessages)

                if (messages.isNotEmpty()) {
                    handleMessages(messages)
                    // 처리 완료된 메시지 삭제
                    aws.sqs.deleteMessageBatch(requestQueueUrl, messages)
                }

                recordCheckInterval.delay()
            }
        } catch (e: Exception) {
            log.error(e) { "SqsWorker 실행 중 오류 발생" }
            throw e
        }
    }

    /**
     * 워커 중지
     */
    suspend fun stop() {
        stopCallback(this)
        log.info { "SqsWorker 종료 중..." }
        isRunning = false
        log.info { "SqsWorker 종료 완료" }
    }

    /**
     * 메시지 처리 핸들러
     * "in" 타입 메시지만 필터링하여 처리 후 "out" 타입으로 전송
     */
    private suspend fun handleMessages(messages: List<Message>) {
        // "in" 타입 메시지만 필터링
        val filteredMessages = messages.filter { SqsTaskRecordKey.isIn(it.messageId!!) }

        if (filteredMessages.isEmpty()) {
            log.debug { " -> 현재 워커에서 처리할 메시지 없음 (타입: in)" }
            return
        }

        log.info { " -> ${filteredMessages.size}개의 메시지 처리 시작 (타입: in)" }

        val records = filteredMessages.map { SqsTaskRecord(it) }
        handler.invoke(records)

        val returnMessages = records.map {
            it.messageKey.outMessageId to it.result.toString()
        }.toMap().values.toList()

        val failedMessages = aws.sqs.sendBatch(resultQueueUrl, returnMessages)

        if (failedMessages.isNotEmpty()) {
            log.warn { "Failed to send ${failedMessages.size} messages to result queue." }
        }

        log.info { " -> ${filteredMessages.size}개의 메시지 처리 완료 & 결과 push 완료" }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}