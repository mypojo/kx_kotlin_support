package net.kotlinx.aws.lambdaCommon.handler

import com.amazonaws.services.lambda.runtime.Context
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.core.gson.GsonData


/**
 * JOB 요청이 오면 실행
 */
class JobHandler(
    private val block: suspend (sqsBody: GsonData) -> Unit
) : LambdaLogicHandler {

    override suspend fun invoke(input: GsonData, context: Context?): Any? {
//        val job = jobConfig.jobSerializer.toJob(input) ?: return null
//        return jobConfig.jobLocalExecutor.runJob(job)
        return null
    }

}
