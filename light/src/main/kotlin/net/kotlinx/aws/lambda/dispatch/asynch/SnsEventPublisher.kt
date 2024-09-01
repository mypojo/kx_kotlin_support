package net.kotlinx.aws.lambda.dispatch.asynch

import com.amazonaws.services.lambda.runtime.Context
import com.google.common.eventbus.EventBus
import mu.KotlinLogging
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.aws.lambda.dispatch.AwsLambdaEvent
import net.kotlinx.aws.lambda.dispatch.LambdaDispatch
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy


data class SnsNotification(val subject: String, val message: String) : AwsLambdaEvent

data class SnsEcsTaskStateChange(val group: String, val stoppedReason: String) : AwsLambdaEvent

data class SnsSfnFail(val jobName: String, val cause: String) : AwsLambdaEvent

data class SnsPipeline(val pipeline: String, val state: String) : AwsLambdaEvent

/**
 * val msg = "${data["Namespace"]} ${data["MetricName"]}\n${sns["NewStateReason"]}"
 * */
data class SnsTrigger(val alarmName: String, val data: GsonData) : AwsLambdaEvent

/** 등록 안된거  */
data class SnsUnknown(val data: GsonData) : AwsLambdaEvent


/**
 * 가능하면 알려진 SNS는 파싱해서 전달한다.;
 * ex) 관리자 슬랙알람 등
 *
 * 이벤트 스키마 dto 매핑 쓰지 않는다.  -> java8 & 잭슨이라서 좋지않다.
 */
class SnsEventPublisher : LambdaDispatch {

    private val log = KotlinLogging.logger {}

    private val bus by koinLazy<EventBus>()

    companion object {
        /** 이거 두문자가 소문자일수도 있나? */
        const val EVENT_SOURCE = "EventSource"
        const val SOURCE_SNS = "aws:sns"
    }

    override suspend fun postOrSkip(input: GsonData, context: Context?): Any? {
        if (input[EVENT_SOURCE].str != SOURCE_SNS) return null

        log.trace { "SNS는 무조건 아래 형식으로 Message 가 포함된다" }
        val msg = GsonData.parse(input["Sns"]["Message"].str!!)
        log.debug { " -> SNS 입력데이터 $msg" } //가능하면 있는게 디버깅하기 좋음

        /** 편의상  sns["Type"].str == "Notification" 이런가 안하고 msg로만 구분함 */
        when {

            /** SNS 노티*/
            msg["detailType"].str != null -> doNotification(msg)

            /** 클라우드와치 알람 */
            msg["AlarmName"].str != null -> {
                val alarmName = msg["AlarmName"].str!!
                val data = msg["Trigger"]
                bus.post(SnsTrigger(alarmName, data))
            }

            /** 간단 메세지 */
            msg["Subject"].str != null -> {
                val subject = msg["Subject"].str!!
                val message = msg["Message"].str!!
                bus.post(SnsNotification(subject, message))
            }

            else -> {
                bus.post(SnsUnknown(msg))
            }

        }

        return LambdaUtil.OK
    }

    private fun doNotification(msg: GsonData) {
        val detailType = msg["detailType"].str
        val detail = msg["detail"]
        when (detailType) {

            /** ECS 상태변경 알림 */
            "ECS Task State Change" -> {
                val stoppedReason = detail["stoppedReason"].str!!
                val group = detail["group"].str!!
                bus.post(SnsEcsTaskStateChange(group, stoppedReason))
            }

            /** SFN 실패 알람 */
            "Step Functions Execution Status Change" -> {
                val jobName = detail["stateMachineArn"].str!!.substringAfterLast(":")
                val msg = detail["cause"].str!!
                bus.post(SnsSfnFail(jobName, msg))
            }

            /** 빌드 알림 */
            "CodePipeline Pipeline Execution State Change" -> {
                val pipeline = detail["pipeline"].str!!
                val state = detail["state"].str!!
                bus.post(SnsPipeline(pipeline, state))
            }

            else -> bus.post(SnsUnknown(msg))

        }
    }


}