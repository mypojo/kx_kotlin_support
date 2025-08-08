package net.kotlinx.aws.kinesis.worker

import aws.sdk.kotlin.services.kinesis.model.Record
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import mu.KotlinLogging
import net.kotlinx.aws.kinesis.reader.KinesisReader
import net.kotlinx.aws.kinesis.reader.gson
import net.kotlinx.calculator.ProgressData
import net.kotlinx.concurrent.delay
import net.kotlinx.json.gson.GsonData
import net.kotlinx.time.TimeStart
import net.kotlinx.time.TimeString
import java.time.LocalDateTime

/**
 * 결과 레코드를 수신해서 flow로 전달해준다
 *
 * kinesis 등의 시스템은, 중복 전달의 가능성이 있음
 * 이때문에 결과를 카운팅으로 처리하지 않고, 실제 ID를 전수 매칭시킴
 *
 * uniqueKeys 10만개 가정 약 10mb 정도 메모리를 차지함
 * */
class KinesisTaskFlow(private val task: KinesisTask, taskId: String, val uniqueKeys: Set<String>) {

    /** flow용 채널 */
    internal val recordChannel = Channel<List<GsonData>>(Channel.BUFFERED)

    /** 수신받은 데이터세트. 코루틴임으로 스래드 안전할 필요는 없음 */
    private val receivedSet = mutableSetOf<String>()

    private var isCompleted = false

    /** 플로우 시작시간 (진행율에 사용) */
    private val startTime = LocalDateTime.now()

    /** task 이름 */
    private val taskFlowName: String = KinesisTaskRecordKey.taskFlowName(task.taskName, taskId)

    /** 최종 건당 처리시간 체크용 */
    private val start = TimeStart()

    /**
     * 이 리더는 매 요청마다 새로 생성된다
     * */
    internal val reader = KinesisReader {
        aws = task.aws
        streamName = task.streamName
        readerName = taskFlowName
        checkpointTableName = task.checkpointTableName
        recordCheckInterval = task.recordCheckInterval
        shardOption = task.shardOption
        readChunkCnt = task.readChunkCnt
        recordHandler = ::handleRecords
        checkpointTtl = task.checkpointTtl
    }

    /**
     * 태스크 실행
     */
    suspend fun startup() {
        // 2. 리더 활성화 (체크포인트 없음)
        log.trace { "리더 활성화 중 (readerName: ${taskFlowName})..." }

        val readerJob = CoroutineScope(Dispatchers.IO).launch {
            reader.start()
        }

        // 리더가 초기화될 시간을 주기 위해 잠시 대기
        delay(2000)

        log.debug { "${taskFlowName} 파티션키 (${taskFlowName}) startup" }

        // 별도의 코루틴에서 완료 처리 및 채널 닫기
        CoroutineScope(Dispatchers.Default).launch {
            try {
                withTimeout(task.timeout) {
                    // 완료될 때까지 대기
                    while (!isCompleted) {
                        task.recordCheckInterval.delay()
                    }
                }

                // 리더 종료
                log.debug { "KinesisTask 종료 중..." }
                reader.stop()
                readerJob.cancelAndJoin()
                recordChannel.close()
                log.info { "${taskFlowName} -> ${receivedSet.size}개의 레코드 처리완 -> ${start} (건당 ${TimeString(start.interval() / receivedSet.size)})" }
            } catch (e: TimeoutCancellationException) {
                log.warn { "${taskFlowName} -> 타임아웃(${task.timeout})으로 인해 태스크가 강제 종료됩니다. 현재까지 받은 레코드 수: ${receivedSet.size} / ${uniqueKeys.size}" }
                reader.stop()
                readerJob.cancelAndJoin()
                recordChannel.close(e) //close시 예외를 전파함!!
            } catch (e: Exception) {
                recordChannel.close(e)
                throw e
            }
        }
    }

    /**
     * 레코드 처리 핸들러
     * out 타입 파티션 데이터만 필터링하여 카운트하고 Flow로 전달
     * @param shardId 샤드 ID
     * @param records 처리할 레코드 목록
     */
    private suspend fun handleRecords(shardId: String, records: List<Record>) {
        // 내꺼 & out 파티션 데이터만 필터링
        val filteredRecords = records.filter { it.partitionKey!!.startsWith(taskFlowName) && KinesisTaskRecordKey.isOut(it.partitionKey!!) }
        if (filteredRecords.isEmpty()) {
            log.debug { " -> ${taskFlowName} #[$shardId] 처리할 레코드(접미어 $taskFlowName) 없음" }
            return
        }

        val gsonDataList = filteredRecords.map { it.gson }

        // 레코드 카운트 증가
        val currentSet = gsonDataList.map { this.task.toId(it) }.toSet()
        receivedSet.addAll(currentSet)
        log.debug { " -> ${taskFlowName} #[$shardId] 레코드 수신현황 ${receivedSet.size} / ${uniqueKeys.size} -> 이중 이번회차 ${filteredRecords.size}건 처리 시작.." }

        recordChannel.send(gsonDataList) // Record 객체를 GsonData 객체로 변환하여 채널에 전송
        log.trace { "${taskFlowName} [$shardId] ${gsonDataList.size}개의 레코드를 Flow로 전달 완료" }

        // 진행률 표시. 깔끔하게 딱 1줄만 인포로 남긴다
        val progressData = ProgressData(uniqueKeys.size, receivedSet.size, startTime)
        log.info { " -> ${taskFlowName} #[$shardId] ${filteredRecords.size}건 -> $progressData" }

        // 모든 응답을 받았는지 확인
        //경고!!! 워커에서 동일한 응답을 두번 줄수도 있기 때문에, 향후에 이부분 인메모리 키 중복 체크로 변경해야함
        if (receivedSet.size >= uniqueKeys.size) {
            isCompleted = true
            log.info { "${taskFlowName} #[$shardId] -> 모든 응답(${receivedSet.size} 개)을 받았습니다. taskFlow를 완료 처리합니다..." }
            check(receivedSet.size == uniqueKeys.size) { "${receivedSet.size} / ${uniqueKeys.size} -> 정확히 일치해야함!!" }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}