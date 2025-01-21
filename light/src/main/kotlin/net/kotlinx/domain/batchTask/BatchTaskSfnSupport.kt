package net.kotlinx.domain.batchTask

import mu.KotlinLogging
import net.kotlinx.domain.batchStep.BatchStepExecutor
import net.kotlinx.domain.job.JobRepository
import net.kotlinx.domain.job.JobUpdateSet
import net.kotlinx.koin.Koins.koin
import net.kotlinx.string.padNumIncrease
import org.koin.core.module.Module
import org.koin.core.qualifier.named

/** BatchTask 등록 (늦은 초기화함) */
fun Module.registBatchTask(id: String, block: BatchTask.() -> Unit) {
    single(named(id)) {
        BatchTask().apply(block).apply {
            this.id = id
        }
    }
}

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
        if (op.batchTaskIds.isNotEmpty()) {
            log.trace { "인메모리 데이터가 입력되는경우 해당 데이터를 S3로 업로드" }
            val inputs = BatchTaskOptionUtil.toS3LogicInputs(op.batchTaskIds, op.datas, op.inputOptionBlock)
            this.upload(sfnId, inputs)
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
