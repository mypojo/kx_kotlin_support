package net.kotlinx.domain.batchTask

import mu.KotlinLogging
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.S3LogicInput
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.S3LogicOutput
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.S3LogicRuntime
import net.kotlinx.collection.flatten
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.toGsonArray
import net.kotlinx.koin.Koins.koinOrCheck
import net.kotlinx.time.TimeStart


/**
 * 배치 작업 실행기
 * S3LogicRuntime 를 구현한다.
 *  */
class BatchTaskExecutor : S3LogicRuntime {

    /** S3LogicRuntime 구현체 */
    override suspend fun executeLogic(inputs: S3LogicInput): S3LogicOutput {
        val logicOption = GsonData.parse(inputs.logicOption)
        val datas = inputs.datas
        val resultMap = executeLogic(logicOption, datas)
        return S3LogicOutput(
            GsonData.fromObj(datas),
            GsonData.fromObj(resultMap),
        )
    }

    /** 일반적인 실행 */
    fun executeLogic(logicOption: GsonData, datas: List<String>): Map<String, GsonData> {
        val batchTaskIds = logicOption[BatchTaskOptionUtil.BATCH_TASK_IDS].map { it.str!! }
        log.info { "로직 ${batchTaskIds.size}건 실행 -> 데이터 ${datas.size}건 -> ${datas.take(2).joinToString(",")}.. " }

        val start = TimeStart()
        val taskResults = batchTaskIds.map { batchTaskId ->
            suspend {
                val each = TimeStart()
                val batchTask = koinOrCheck<BatchTask>(batchTaskId)
                log.debug { " -> 로직 [${batchTask.name}] 실행 시작.." }
                batchTask.runtime.executeLogic(logicOption, datas).apply {
                    log.info { " -> 로직 [${batchTask.name}] 종료 -> $each" }
                }
            }
        }.coroutineExecute()

        val jobResults = taskResults.flatten().map { it.key to it.value.toGsonArray() }.toMap()
        log.info { "전체로직 ${batchTaskIds.size}건 완료 -> 전체 걸린시간 : $start" }
        return jobResults
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }


}