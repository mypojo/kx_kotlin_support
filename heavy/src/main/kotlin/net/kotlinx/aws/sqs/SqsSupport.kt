package net.kotlinx.aws.sqs

import aws.sdk.kotlin.services.sqs.SqsClient
import aws.sdk.kotlin.services.sqs.deleteMessageBatch
import aws.sdk.kotlin.services.sqs.model.DeleteMessageBatchRequestEntry
import aws.sdk.kotlin.services.sqs.model.Message
import aws.sdk.kotlin.services.sqs.model.SendMessageBatchRequestEntry
import aws.sdk.kotlin.services.sqs.receiveMessage
import aws.sdk.kotlin.services.sqs.sendMessageBatch


/** 디폴트 최대 수. 기본설정이 1임..  */
private const val MAX_NUMBER_OF_MESSAGES = 10

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

/** 메세지 간단 가져오기 */
suspend fun SqsClient.receiveMessage(queueUrl: String, maxNum: Int = MAX_NUMBER_OF_MESSAGES): List<Message> {
    check(maxNum <= MAX_NUMBER_OF_MESSAGES)
    return this.receiveMessage {
        this.queueUrl = queueUrl
        this.maxNumberOfMessages = maxNum
    }.messages!!
}

/**
 * async면 오래걸림
 * 주의!  표시 제한 시간이 0이면 무한 로드 될거임. 무한로드 방지 로직이 포함됨
 * 반드시 전체 내용이 필요한 로직 등, 제한적으로 사용해야함!!
 */
suspend fun SqsClient.receiveMessageAll(queueUrl: String, maxRepeatCnt: Int = 20): List<Message> {
    return ArrayList<Message>().also { list ->
        val unique: MutableSet<String> = mutableSetOf()
        repeat(maxRepeatCnt) {
            val receiveMsgs: List<Message> = receiveMessage(queueUrl)
            if (receiveMsgs.isEmpty()) return list

            val duplicated = receiveMsgs.filter { unique.add(it.messageId!!) }
            if (duplicated.isNotEmpty()) {
                //log.warn { "중복 데이터가 발견되었습니다. 큐 옵션의 '표시 제한 시간' 을 늘려주세요 : $duplicated " }
                return list
            }
            //log.debug { "  --> 데이터 로드 ${receiveMsgs.size}건" }
            list.addAll(receiveMsgs)
        }
    }

}