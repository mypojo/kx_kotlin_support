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

data class EventBridgeSchedulerEvent(
    /** 스케쥴 그룹명 */
    val groupName: String,
    /** 스캐줄 명 */
    val scheduleName: String,
) : AwsLambdaEvent

data class EventBridgeEcsTaskStateChange(val group: String, val stoppedReason: String) : AwsLambdaEvent

data class EventBridgeSfnStatus(
    val sfnName: String,
    /** job의 ${pk}-${sk} */
    val name: String,
    val status: String, val startDate: Long, val input: String, val output: String, val cause: String
) : AwsLambdaEvent

data class EventBridgePipeline(val pipeline: String, val state: String) : AwsLambdaEvent

data class EventBridgeUnknown(val data: GsonData) : AwsLambdaEvent

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

        val detailType = input[AwsNaming.EventBridge.DETAIL_TYPE].str ?: input[AwsNaming.EventBridge.DETAIL_TYPE_SNS].str!!
        val detail = input["detail"]
        return when (source) {

            "aws.scheduler" -> {
                //ex) arn:aws:scheduler:ap-northeast-2:99999999:schedule/{groupName}/{scheduleName}
                val resourceArn: String = input["resources"][0].str!!
                val scheduleInfo = resourceArn.substringAfter("/").split("/")
                check(scheduleInfo.size == 2)
                log.debug { "[event] ARN = $resourceArn  =>  $scheduleInfo" }
                bus.postEvent { EventBridgeSchedulerEvent(scheduleInfo[0], scheduleInfo[1]) }
            }

            "aws.states" -> {
                when (detailType) {
                    /** SFN 결과알림 */
                    "Step Functions Execution Status Change" -> {
                        val sfnName = detail["stateMachineArn"].str!!.substringAfterLast(":")
                        val name = detail["name"].str!!
                        val status = detail["status"].str!!
                        val startDate = detail["startDate"].str!!.toLong()
                        val input = detail["input"].str ?: "-"
                        val output = detail["output"].str ?: "-"
                        val cause = detail["cause"].str ?: "-"
                        bus.postEvent { EventBridgeSfnStatus(sfnName, name, status, startDate, input, output, cause) }
                    }

                    else -> {
                        bus.postEvent { EventBridgeUnknown(input) }
                    }
                }
            }

            "aws.codepipeline" -> {
                when (detailType) {
                    /** 빌드 알림 */
                    "CodePipeline Pipeline Execution State Change" -> {
                        val pipeline = detail["pipeline"].str!!
                        val state = detail["state"].str!!
                        bus.postEvent({ EventBridgePipeline(pipeline, state) })
                    }

                    else -> {
                        bus.postEvent { EventBridgeUnknown(input) }
                    }
                }
            }


            "aws.ecs" -> {
                when (detailType) {
                    /** ECS 상태변경 알림 */
                    "ECS Task State Change" -> {
                        val stoppedReason = detail["stoppedReason"].str!!
                        val group = detail["group"].str!!
                        bus.postEvent { EventBridgeEcsTaskStateChange(group, stoppedReason) }
                    }

                    else -> {
                        bus.postEvent { EventBridgeUnknown(input) }
                    }
                }
            }

            else -> {
                bus.postEvent { EventBridgeUnknown(input) }
            }
        }
    }


}