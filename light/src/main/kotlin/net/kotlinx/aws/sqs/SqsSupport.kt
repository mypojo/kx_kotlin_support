package net.kotlinx.aws.sqs

import aws.sdk.kotlin.services.sqs.*
import aws.sdk.kotlin.services.sqs.model.*
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist
import net.kotlinx.collection.doUntilNotEmpty
import java.util.*

val AwsClient.sqs: SqsClient
    get() = getOrCreateClient { SqsClient { awsConfig.build(this) }.regist(awsConfig) }

/** 디폴트 최대 수. 기본설정이 1임..  */
private const val MAX_NUMBER_OF_MESSAGES = 10

/**
 * FIFO 단건전송 샘플
 * @param queueUrl 타계정으로 전송시  https 로 시작하는 URL 입력
 *  */
suspend fun SqsClient.sendFifo(queueUrl: String, messageGroupId: String, body: Any, uid: String = UUID.randomUUID().toString()): SendMessageResponse {
    return this.sendMessage {
        this.queueUrl = queueUrl
        this.messageGroupId = messageGroupId
        this.messageBody = body.toString()
        this.messageDeduplicationId = uid
    }
}

/**
 * 여러개 보낼때. 오류건을 리턴한다.
 * @return 오류난것들
 *  */
suspend fun SqsClient.sendBatch(queueUrl: String, messages: Collection<String>): List<String> {
    val msgMap = messages.mapIndexed { index, message -> "$index" to message }.toMap() //ID 추가
    return msgMap.entries.chunked(MAX_NUMBER_OF_MESSAGES).flatMap { splited ->
        this.sendMessageBatch {
            this.queueUrl = queueUrl
            this.entries = splited.map {
                SendMessageBatchRequestEntry {
                    this.id = it.key
                    this.messageBody = it.value
                }
            }
        }.failed.map { it.message!! }
    }
}

/**
 * 벌크 삭제.
 * @return 오류난것들
 *  */
suspend fun SqsClient.deleteMessageBatch(queueUrl: String, messages: Collection<Message>): List<Message> {
    val msgMap = messages.mapIndexed { index, message -> "$index" to message }.toMap() //ID 추가
    return msgMap.entries.chunked(MAX_NUMBER_OF_MESSAGES).flatMap { splited ->
        this.deleteMessageBatch {
            this.queueUrl = queueUrl
            this.entries = splited.map {
                DeleteMessageBatchRequestEntry {
                    this.id = it.key
                    this.receiptHandle = it.value.receiptHandle
                }
            }
        }.failed.map { msgMap[it.id]!! } //id만 전달되서 일케 처리했음
    }
}

//==================================================== 가져오기 ======================================================

suspend fun SqsClient.receiveMessage(
    queueUrl: String,
    visibilityTimeout: Int? = null,
    maxNum: Int = MAX_NUMBER_OF_MESSAGES,
    waitTimeSeconds: Int = 20  // 롱 폴링을 위한 파라미터 추가
): List<Message> {
    check(maxNum <= MAX_NUMBER_OF_MESSAGES)
    visibilityTimeout?.let {
        check(it > 0) { "가시성은 0보다 커야함! 무한루프가 되는 수 있음" }
    }
    check(waitTimeSeconds in 0..20) { "waitTimeSeconds는 0에서 20 사이여야 합니다" }

    return this.receiveMessage {
        this.queueUrl = queueUrl
        this.maxNumberOfMessages = maxNum
        this.visibilityTimeout = visibilityTimeout
        this.waitTimeSeconds = waitTimeSeconds  // 롱 폴링 설정
        this.messageSystemAttributeNames = listOf(
            MessageSystemAttributeName.SentTimestamp,
        )
    }.messages ?: emptyList()
}

/**
 * async면 오래걸림
 * 주의!  표시 제한 시간이 0이면 무한 로드 될거임. 무한로드 방지 로직이 포함됨
 * 반드시 전체 내용이 필요한 로직 등, 제한적으로 사용해야함!!
 */
@Deprecated("첨 만들때 잘못 만든거같다..")
suspend fun SqsClient.receiveMessageAll(queueUrl: String, visibilityTimeout: Int? = null, limitCnt: Int = 10): List<Message> = doUntilNotEmpty {
    if (it >= limitCnt) emptyList()
    else receiveMessage(queueUrl, visibilityTimeout)
}.flatten().distinctBy { it.messageId!! }