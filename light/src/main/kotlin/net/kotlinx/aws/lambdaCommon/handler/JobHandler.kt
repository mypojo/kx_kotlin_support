package net.kotlinx.aws.lambdaCommon.handler

import com.amazonaws.services.lambda.runtime.Context
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.core.gson.GsonData
import net.kotlinx.module.job.trigger.JobTrigger


/**
 * JOB 요청이 오면 실행
 */
class JobHandler(
    private val jobTrigger: JobTrigger,
    private val block: suspend (sqsBody: GsonData) -> Unit
) : LambdaLogicHandler {

    override suspend fun invoke(input: GsonData, context: Context?): Any? {
        val job = jobTrigger.jobSerializer.toJob(input) ?: return null
        return jobTrigger.jobLocalExecutor.runJob(job)
    }

}
