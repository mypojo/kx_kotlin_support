package net.kotlinx.aws.lambda.dispatch.asynch

import com.amazonaws.services.lambda.runtime.Context
import com.google.common.eventbus.EventBus
import mu.KotlinLogging
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.lambda.dispatch.AwsLambdaEvent
import net.kotlinx.aws.lambda.dispatch.LambdaDispatch
import net.kotlinx.guava.postEvent
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy

/**
 * AWS 이벤트브릿지
 * */
class AwsEventBridgePublisher : LambdaDispatch {

    private val log = KotlinLogging.logger {}

    private val bus by koinLazy<EventBus>()

    override suspend fun postOrSkip(input: GsonData, context: Context?): Any? {
        return doEventBridge(input)
    }

    fun doEventBridge(input: GsonData): AwsLambdaEvent? {
        log.trace { "소스 필드 여부로 이벤트브릿지인지 판단한다" }
        val source = input[AwsNaming.EventBridge.SOURCE].str ?: return null
        if (!source.startsWith("aws.")) return null

        val body = EventBridgeJson(input)
        val detailType = body.detailType
        return when (source) {
            "aws.scheduler" -> bus.postEvent { EventBridgeSchedulerEvent(body) }

            "aws.states" -> {
                when (detailType) {
                    /** SFN 결과알림 */
                    "Step Functions Execution Status Change" -> bus.postEvent { EventBridgeSfnStatus(body) }
                    else -> bus.postEvent { body }
                }
            }

            "aws.codepipeline" -> {
                when (detailType) {
                    "CodePipeline Pipeline Execution State Change" -> bus.postEvent { EventBridgePipeline(body) }
                    else -> bus.postEvent { body }
                }
            }


            "aws.ecs" -> {
                when (detailType) {
                    "ECS Task State Change" -> bus.postEvent { EventBridgeEcsTaskStateChange(body) }
                    else -> bus.postEvent { body }
                }
            }

            "aws.batch" -> {
                when (detailType) {
                    "Batch Job State Change" -> bus.postEvent { EventBridgeAwsBatchStateChange(body) }
                    else -> bus.postEvent { body }
                }
            }

            "aws.s3" -> {
                bus.postEvent { EventBridgeS3(body) }
            }

            else -> {
                bus.postEvent { body }
            }
        }
    }


}