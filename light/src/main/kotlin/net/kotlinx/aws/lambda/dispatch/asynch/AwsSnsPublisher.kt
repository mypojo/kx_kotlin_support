package net.kotlinx.aws.lambda.dispatch.asynch

import com.amazonaws.services.lambda.runtime.Context
import com.google.common.eventbus.EventBus
import mu.KotlinLogging
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.aws.lambda.dispatch.LambdaDispatch
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy


/**
 * 가능하면 알려진 SNS는 파싱해서 전달한다.
 * ex) 관리자 슬랙알람 등
 *
 * 이벤트 스키마 dto 매핑 쓰지 않는다.  -> java8 & 잭슨이라서 좋지않다.
 *
 * 가능하면 이벤트브릿지를 다이렉트 람다로 연결할것!! -> SNS 거치면 코드가 지저분해짐
 * 무조건 SNS만 되는거 -> 코드 파이프라인 등 => 완전 커스텀 기능이라 챗봇하고 SNS만 됨
 */
class AwsSnsPublisher : LambdaDispatch {

    private val log = KotlinLogging.logger {}

    private val bus by koinLazy<EventBus>()

    private val eventBridge = AwsEventBridgePublisher()

    companion object {
        const val SOURCE_SNS = "aws:sns"
    }


    override suspend fun postOrSkip(input: GsonData, context: Context?): Any? {
        if (input[AwsNaming.Event.EVENT_SOURCE2].str != SOURCE_SNS) return null

        val topicName = input["EventSubscriptionArn"].str!!.substringAfterLast(":")

        log.trace { "SNS는 무조건 아래 형식으로 Message 가 포함된다" }
        val sns = input["Sns"]
        val msg = GsonData.parse(sns["Message"].str!!) //메세지는 일반 텍스트라 따로 파싱해줘야함

        log.trace { "이벤트 브릿지가 토스된거라면 이벤트브릿지에서 처리" }
        eventBridge.doEventBridge(msg)?.let { return it }

        log.debug { " -> $topicName SNS 입력 메세지 $msg" } //가능하면 있는게 디버깅하기 좋음
        /** 편의상  sns["Type"].str == "Notification" 이런가 안하고 msg로만 구분함 */
        when {

            /** 클라우드와치 알람 */
            msg["AlarmName"].str != null -> {
                val alarmName = msg["AlarmName"].str!!
                val data = msg["Trigger"]
                bus.post(SnsAlarm(alarmName, data))
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


}