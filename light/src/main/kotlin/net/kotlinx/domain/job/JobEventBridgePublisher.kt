package net.kotlinx.domain.job

import com.amazonaws.services.lambda.runtime.Context
import com.google.common.eventbus.EventBus
import mu.KotlinLogging
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.lambda.dispatch.AwsLambdaEvent
import net.kotlinx.aws.lambda.dispatch.LambdaDispatch
import net.kotlinx.aws.lambda.dispatch.asynch.EventBridgeUnknown
import net.kotlinx.guava.postEvent
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy

/** 잡 이벤트브릿지 - 상태변경 */
data class EventBridgeJobStatus(val job: Job) : AwsLambdaEvent {

    companion object {
        const val DETAIL_TYPE = "Job Status Change"
    }
}

/**
 * AWS 이벤트브릿지
 * */
class JobEventBridgePublisher : LambdaDispatch {

    private val bus by koinLazy<EventBus>()

    override suspend fun postOrSkip(input: GsonData, context: Context?): Any? {
        return doEventBridge(input)
    }

    fun doEventBridge(input: GsonData): AwsLambdaEvent? {
        log.trace { "소스 필드 여부로 이벤트브릿지인지 판단한다" }
        val source = input[AwsNaming.EventBridge.SOURCE].str ?: return null
        if (source != SOURCE) return null

        val detailType = input[AwsNaming.EventBridge.DETAIL_TYPE].str ?: input[AwsNaming.EventBridge.DETAIL_TYPE_SNS].str!!
        val detail = input["detail"]
        return when (detailType) {

            EventBridgeJobStatus.DETAIL_TYPE -> {
                val job = detail.fromJson<Job>()
                bus.postEvent { EventBridgeJobStatus(job) }
            }

            else -> {
                bus.postEvent { EventBridgeUnknown(input) }
            }
        }
    }

    companion object {

        private val log = KotlinLogging.logger {}

        const val SOURCE = "kotlinx.job"
    }


}