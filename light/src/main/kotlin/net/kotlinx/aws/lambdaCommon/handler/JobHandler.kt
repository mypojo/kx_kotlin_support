package net.kotlinx.aws.lambdaCommon.handler

import com.amazonaws.services.lambda.runtime.Context
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.core.gson.GsonData
import net.kotlinx.module.job.trigger.JobLocalExecutor
import net.kotlinx.module.job.trigger.JobSerializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.get


/**
 * JOB 요청이 오면 실행
 */
class JobHandler : LambdaLogicHandler, KoinComponent {

    /** 혹시 몰라서, 동적으로 다 가져옴 */
    override suspend fun invoke(input: GsonData, context: Context?): Any? {

        val jobSerializer = get<JobSerializer>()
        val job = jobSerializer.toJob(input) ?: return null

        val jobLocalExecutor = get<JobLocalExecutor>()
        return jobLocalExecutor.runJob(job)
    }

}
