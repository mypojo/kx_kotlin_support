package net.kotlinx.domain.batchStep

import net.kotlinx.domain.job.Job

/**
 * BatchStep 전후 콜백
 * koin으로 등록해서 사용
 *
 * job pk로 콜백이 등록되어있을경우 StepStart 등이 끝날때 호출됨
 * */
interface BatchStepCallback {

    suspend fun execute(option: BatchStepOption, job: Job)

}


