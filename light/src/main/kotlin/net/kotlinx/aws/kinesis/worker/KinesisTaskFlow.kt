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
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger


class KinesisTaskFlow(private val task: KinesisTask, val taskId: String, val totalCnt: Int) {

    internal val recordChannel = Channel<List<GsonData>>(Channel.BUFFERED)

    private val receivedCount = AtomicInteger(0)
    private var isCompleted = false

    /** 플로우 시작시간 (진행율에 사용) */
    private val startTime = LocalDateTime.now()

    /** 요청 파티션 키 (taskName-taskId-in) */
    internal val inPartitionKey: String
        get() = "${task.taskName}-$taskId-in"

    /** 응답 파티션 키 (taskName-taskId-out) */
    internal val outPartitionKey: String
        get() = "${task.taskName}-$taskId-out"

    /** 리더 이름 (task-taskId) */
    internal val taskReaderName: String
        get() = "${task.taskName}-$taskId"

    /**
     * 이 리더는 매 요청마다 새로 생성된다
     * */
    internal val reader = KinesisReader {
        aws = task.aws
        streamName = task.streamName
        readerName = taskReaderName
        checkpointTableName = task.checkpointTableName
        recordCheckInterval = task.recordCheckInterval
        shardCheckInterval = task.shardCheckInterval
        readChunkCnt = task.readChunkCnt
        recordHandler = { shardId, records ->
            handleRecords(shardId, records)
        }
        checkpointTtl = task.checkpointTtl
    }

    /**
     * 태스크 실행
     */
    suspend fun startup() {
        // 2. 리더 활성화 (체크포인트 없음)
        log.trace { "리더 활성화 중 (readerName: $taskReaderName)..." }

        val readerJob = CoroutineScope(Dispatchers.IO).launch {
            reader.start()
        }

        // 리더가 초기화될 시간을 주기 위해 잠시 대기
        delay(2000)

        log.info { "${taskReaderName} 파티션키 (${outPartitionKey}) 응답을 대기.." }

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
                log.info { "${taskReaderName} -> ${receivedCount.get()}개의 레코드 처리 완료 & task 정상 종료" }
            } catch (e: TimeoutCancellationException) {
                log.warn { "${taskReaderName} -> 타임아웃(${task.timeout})으로 인해 태스크가 강제 종료됩니다. 현재까지 받은 레코드 수: ${receivedCount.get()} / $totalCnt" }
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
        // out 파티션 데이터만 필터링
        val filteredRecords = records.filter { it.partitionKey == outPartitionKey }
        if (filteredRecords.isEmpty()) {
            log.debug { " -> ${taskReaderName} #[$shardId] 처리할 레코드($outPartitionKey) 없음" }
            return
        }

        log.debug { " -> ${taskReaderName} #[$shardId] 대상 파티션 $outPartitionKey ->  ${filteredRecords.size}개의 레코드 처리.." }

        // 레코드 카운트 증가
        val currentCount = receivedCount.addAndGet(filteredRecords.size)
        log.debug { " -> ${taskReaderName} #[$shardId] 레코드 수신현황 $currentCount / $totalCnt -> 이번회차 ${filteredRecords.size} 처리 시작.." }

        // Record 객체를 GsonData 객체로 변환하여 채널에 전송
        val gsonDataList = filteredRecords.map { it.gson }
        recordChannel.send(gsonDataList)
        log.trace { "${taskReaderName} [$shardId] ${gsonDataList.size}개의 레코드를 Flow로 전달 완료" }

        // 진행률 표시. 깔끔하게 딱 1줄만 인포로 남긴다
        val progressData = ProgressData(totalCnt.toLong(), currentCount.toLong(), startTime)
        log.info { " -> ${taskReaderName} #[$shardId] ${filteredRecords.size}건 -> $progressData" }

        // 모든 응답을 받았는지 확인
        if (currentCount >= totalCnt) {
            isCompleted = true
            log.info { "${taskReaderName} #[$shardId] -> 모든 응답($currentCount 개)을 받았습니다. taskFlow를 완료 처리합니다..." }
            check(currentCount == totalCnt) { "${currentCount} / ${totalCnt} -> 정확히 일치해야함!!" }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }
}