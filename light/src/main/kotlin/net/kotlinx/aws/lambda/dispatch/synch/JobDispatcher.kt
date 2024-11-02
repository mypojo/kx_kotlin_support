package net.kotlinx.aws.lambda.dispatch.synch

import com.amazonaws.services.lambda.runtime.Context
import net.kotlinx.aws.lambda.dispatch.LambdaDispatch
import net.kotlinx.domain.job.JobStatus
import net.kotlinx.domain.job.trigger.JobLocalExecutor
import net.kotlinx.domain.job.trigger.JobSerializer
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy


/**
 * JOB 요청이 오면 실행
 */
class JobDispatcher : LambdaDispatch {

    private val jobSerializer by koinLazy<JobSerializer>()
    private val jobLocalExecutor by koinLazy<JobLocalExecutor>()

    override suspend fun postOrSkip(input: GsonData, context: Context?): Any? {
        val job = jobSerializer.toJob(input) ?: return null
        check(job.jobStatus.readyToRun()) { "최초 등록된 잡은 실행 가능한 상태여야 합니다" }

        if (job.jobStatus == JobStatus.RESERVED) return job.toKeyString()

        return jobLocalExecutor.execute(job)
    }

}
