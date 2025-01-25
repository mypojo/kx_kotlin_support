package net.kotlinx.domain.batchTask.sfn

import mu.KotlinLogging
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.S3LogicInput
import net.kotlinx.domain.batchStep.BatchStepExecutor
import net.kotlinx.domain.batchTask.BatchTaskExecutor
import net.kotlinx.domain.batchTask.BatchTaskOptionUtil
import net.kotlinx.domain.job.JobRepository
import net.kotlinx.domain.job.JobUpdateSet
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koin
import net.kotlinx.reflect.name
import net.kotlinx.string.padNumIncrease

/**
 * 간단버전
 * 인메모리 데이터를 업로드 후 실행
 * */
suspend fun BatchStepExecutor.startExecution(op: BatchTaskSfn) {

    val log = KotlinLogging.logger {}

    //명시적으로 DDB 입력값을 기준으로 리트라이 인지 아닌지를 구분함
    if (op.job.sfnId == null) {
        //==================================================== 첫 시도 ======================================================
        val sfnId = op.parameter.option.sfnId
        op.batchTaskInputInmemery?.let {
            log.info { "인메모리 데이터가 입력되는경우 해당 데이터를 S3로 업로드" }
            this.uploadAllInmemory(sfnId, it.toS3LogicInputs())
        }
        op.batchTaskInputCsv?.let { csv ->
            log.info { "CSV 데이터가 입력되는경우 해당 데이터를 청크단위로 읽어서 json array 형태로 S3로 업로드" }
            val tool = csv.csvReadWriteTools
            val inputOption = BatchTaskOptionUtil.inputOption(csv.batchTaskIds, csv.inputOptionBlock)
            tool.processor = {
                val lines = it.rows.map { GsonData.fromObj(it).toString() } //CSV 라인을 json array로
                val logicInput = S3LogicInput(BatchTaskExecutor::class.name(), lines, inputOption)
                this.upload(sfnId, it.index, logicInput)
            }
            tool.run()
        }

        op.job.sfnId = sfnId
        op.job.lastSfnId = sfnId
        koin<JobRepository>().updateItem(op.job, JobUpdateSet.SFN)
        this.startExecution(op.parameter)
    } else {
        //==================================================== 재시도는 별도의 업로드나 설정세팅 필요없음 ======================================================
        val oldSfnId = op.job.lastSfnId ?: throw IllegalStateException("lastSfnId is null")
        val newSfnId = oldSfnId.padNumIncrease("-R", 3)
        op.job.lastSfnId = newSfnId //다음 재시도를 위해서 SFN_ID를 정보를 교체해줌
        koin<JobRepository>().updateItem(op.job, JobUpdateSet.SFN)
        this.startExecutionRetry(oldSfnId, newSfnId)
    }


}
