package net.kotlinx.domain.batchStep

import net.kotlinx.domain.job.Job

/**
 * BatchStep 전후 콜백
 * koin으로 등록해서 사용
 * */
interface BatchStepCallback {

    suspend fun execute(option: BatchStepOption, job: Job)

}


