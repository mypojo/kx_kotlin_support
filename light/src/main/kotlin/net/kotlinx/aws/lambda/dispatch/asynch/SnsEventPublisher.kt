package net.kotlinx.aws.lambda.dispatch.asynch

import com.amazonaws.services.lambda.runtime.Context
import com.google.common.eventbus.EventBus
import mu.KotlinLogging
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.aws.lambda.dispatch.LambdaDispatch
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy


data class SnsNotification(val subject: String, val message: String)

data class SnsEcsTaskStateChange(val group: String, val stoppedReason: String)

data class SnsSfnFail(val jobName: String, val cause: String)

data class SnsPipeline(val pipeline: String, val state: String)

/**
 * val msg = "${data["Namespace"]} ${data["MetricName"]}\n${sns["NewStateReason"]}"
 * */
data class SnsTrigger(val alarmName: String, val data: GsonData)

/** 등록 안된거  */
data class SnsUnknown(val data: GsonData)


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

        val connverted = input["Sns"].let { sns ->
            /** JSON 그 자체가 메세지인 경우가 있고, 단순 텍스트(Budget Notification 등)인 경우도 있다 */
            try {
                val body = sns["Message"].str!!
                GsonData.parse(body)
            } catch (e: Exception) {
                sns
            }
        }

        doParseString(connverted)
        doParseJson(connverted)

        return LambdaUtil.OK
    }

    /** String 형태 알림인 경우 */
    private fun doParseString(sns: GsonData) {
        val type = sns["Type"].str ?: return
        check(type == "Notification")

        val subject = sns["Subject"].str!!
        val message = sns["Message"].str!!
        bus.post(SnsNotification(subject, message))
    }

    /** JSON 형태 알림인 경우 */
    private fun doParseJson(sns: GsonData) {
        val detailType = sns["detail-type"].str ?: return


        when (detailType) {

            /** ECS 상태변경 알림 */
            "ECS Task State Change" -> {
                val detail = sns["detail"]
                val stoppedReason = detail["stoppedReason"].str!!
                val group = detail["group"].str!!
                bus.post(SnsEcsTaskStateChange(group, stoppedReason))
            }

            /** SFN 실패 알람 */
            "Step Functions Execution Status Change" -> {
                val detail = sns["detail"]
                val jobName = detail["stateMachineArn"].str!!.substringAfterLast(":")
                val msg = detail["cause"].str!!
                bus.post(SnsSfnFail(jobName, msg))
            }

            /** 빌드 알림 */
            "CodePipeline Pipeline Execution State Change" -> {
                val detail = sns["detail"]
                val pipeline = detail["pipeline"].str!!
                val state = detail["state"].str!!
                bus.post(SnsPipeline(pipeline, state))
            }

            else -> {

                //클라우드 와치 알람 먼저 체크
                when (val alarmName = sns["AlarmName"].str) {
                    "unknown" -> {
                        //이거 알람별로 수정확인
                        val data = sns["Trigger"]
                        bus.post(SnsTrigger(alarmName, data))
                    }

                    else -> {
                        bus.post(SnsUnknown(sns))
                    }
                }
            }
        }
    }

}