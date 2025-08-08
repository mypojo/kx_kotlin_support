package net.kotlinx.aws.sqs.worker

import aws.sdk.kotlin.services.sqs.model.Message
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import net.kotlinx.aws.sqs.deleteMessageBatch
import net.kotlinx.aws.sqs.receiveMessage
import net.kotlinx.aws.sqs.sqs
import net.kotlinx.calculator.ProgressData
import net.kotlinx.concurrent.delay
import net.kotlinx.json.gson.GsonData
import net.kotlinx.time.TimeStart
import net.kotlinx.time.TimeString
import java.time.LocalDateTime

/**
 * 결과 메시지를 수신해서 flow로 전달해준다
 *
 * SQS는 중복 전달의 가능성이 있음
 * 이때문에 결과를 카운팅으로 처리하지 않고, 실제 ID를 전수 매칭시킴
 *
 * uniqueKeys 10만개 가정 약 10mb 정도 메모리를 차지함
 * */
class SqsTaskFlow(private val task: SqsTask, taskId: String, val uniqueKeys: Set<String>) {

    /** flow용 채널 */
    internal val recordChannel = Channel<List<GsonData>>(Channel.BUFFERED)

    /** 수신받은 데이터세트. 코루틴임으로 스래드 안전할 필요는 없음 */
    private val receivedSet = mutableSetOf<String>()

    private var isCompleted = false

    /** 플로우 시작시간 (진행율에 사용) */
    private val startTime = LocalDateTime.now()

    /** task 이름 */
    private val taskFlowName: String = SqsTaskRecordKey.taskFlowName(task.taskName, taskId)

    /** 최종 건당 처리시간 체크용 */
    private val start = TimeStart()

    /**
     * 태스크 실행
     */
    suspend fun startup() {
        log.trace { "SqsTaskFlow 활성화 중 (taskFlowName: ${taskFlowName})..." }

        // 별도의 코루틴에서 완료 처리 및 채널 닫기
        CoroutineScope(Dispatchers.Default).launch {
            try {
                withTimeout(task.timeout) {
                    // 완료될 때까지 대기하면서 결과 큐에서 메시지 폴링
                    while (!isCompleted) {
                        val messages = task.aws.sqs.receiveMessage(task.resultQueueUrl, task.visibilityTimeout, task.maxNumberOfMessages)
                        if (messages.isNotEmpty()) {
                            handleMessages(messages)
                            // 처리 완료된 메시지 삭제
                            task.aws.sqs.deleteMessageBatch(task.resultQueueUrl, messages)
                        }
                        task.recordCheckInterval.delay()
                    }
                }

                recordChannel.close()
                log.info { "${taskFlowName} -> ${receivedSet.size}개의 레코드 처리완료 -> ${start} (건당 ${TimeString(start.interval() / receivedSet.size)})" }
            } catch (e: TimeoutCancellationException) {
                log.warn { "${taskFlowName} -> 타임아웃(${task.timeout})으로 인해 태스크가 강제 종료됩니다. 현재까지 받은 레코드 수: ${receivedSet.size} / ${uniqueKeys.size}" }
                recordChannel.close(e) //close시 예외를 전파함!!
            } catch (e: Exception) {
                recordChannel.close(e)
                throw e
            }
        }
    }

    /**
     * 메시지 처리 핸들러
     * out 타입 메시지만 필터링하여 카운트하고 Flow로 전달
     * @param messages 처리할 메시지 목록
     */
    private suspend fun handleMessages(messages: List<Message>) {
        // 내꺼 & out 타입 메시지만 필터링
        val filteredMessages = messages.filter { 
            it.messageId!!.startsWith(taskFlowName) && SqsTaskRecordKey.isOut(it.messageId!!) 
        }
        
        if (filteredMessages.isEmpty()) {
            log.debug { " -> ${taskFlowName} 처리할 메시지(접미어 $taskFlowName) 없음" }
            return
        }

        val gsonDataList = filteredMessages.map { GsonData.parse(it.body!!) }

        // 레코드 카운트 증가
        val currentSet = gsonDataList.map { task.toId(it) }.toSet()
        receivedSet.addAll(currentSet)
        log.debug { " -> ${taskFlowName} 메시지 수신현황 ${receivedSet.size} / ${uniqueKeys.size} -> 이중 이번회차 ${filteredMessages.size}건 처리 시작.." }

        recordChannel.send(gsonDataList) // 메시지를 GsonData 객체로 변환하여 채널에 전송
        log.trace { "${taskFlowName} ${gsonDataList.size}개의 메시지를 Flow로 전달 완료" }

        // 진행률 표시. 깔끔하게 딱 1줄만 인포로 남긴다
        val progressData = ProgressData(uniqueKeys.size, receivedSet.size, startTime)
        log.info { " -> ${taskFlowName} ${filteredMessages.size}건 -> $progressData" }

        // 모든 응답을 받았는지 확인
        if (receivedSet.size >= uniqueKeys.size) {
            isCompleted = true
            log.info { "${taskFlowName} -> 모든 응답(${receivedSet.size} 개)을 받았습니다. taskFlow를 완료 처리합니다..." }
            check(receivedSet.size == uniqueKeys.size) { "${receivedSet.size} / ${uniqueKeys.size} -> 정확히 일치해야함!!" }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}