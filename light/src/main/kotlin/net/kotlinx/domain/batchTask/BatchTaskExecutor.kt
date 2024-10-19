package net.kotlinx.domain.batchTask

import mu.KotlinLogging
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.S3LogicInput
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.S3LogicOutput
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.S3LogicRuntime
import net.kotlinx.collection.flatten
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.toGsonArray
import net.kotlinx.koin.Koins.koin
import net.kotlinx.time.TimeStart


/**
 * 배치 작업 실행기
 * S3LogicRuntime 를 구현한다.
 *  */
class BatchTaskExecutor : S3LogicRuntime {

    /** S3LogicRuntime 구현체 */
    override suspend fun executeLogic(input: S3LogicInput): S3LogicOutput {
        val logicOption = GsonData.parse(input.logicOption)
        val datas = input.datas
        val resultMap = executeLogic(logicOption, datas)
        return S3LogicOutput(
            GsonData.fromObj(datas),
            GsonData.fromObj(resultMap),
        )
    }

    fun executeLogic(logicOption: GsonData, datas: List<String>): Map<String, GsonData> {
        val batchTaskIds = logicOption[BatchTaskOptionUtil.BATCH_TASK_IDS].map { it.str!! }
        log.info { "로직 ${batchTaskIds.size}건 실행 -> 데이터 ${datas.size}건 -> ${datas.take(2).joinToString(",")}.. " }

        val start = TimeStart()
        val taskResults = batchTaskIds.map { batchTaskId ->
            suspend {
                val batchTask = koin<BatchTask>(batchTaskId)
                log.debug { " -> 로직 ${batchTask.id} 병렬 실행.." }
                batchTask.runtime.executeLogic(datas, logicOption)
            }
        }.coroutineExecute()

        val jobResults = taskResults.flatten().map { it.key to it.value.toGsonArray() }.toMap()
        log.info { "로직 ${batchTaskIds.size}건 완료 -> $start" }
        return jobResults
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }


}