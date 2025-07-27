package net.kotlinx.kotest.modules.lambdaDispatcher

import com.google.common.eventbus.Subscribe
import mu.KotlinLogging
import net.kotlinx.aws.lambda.dispatch.asynch.AwsSnsAlarm
import net.kotlinx.aws.lambda.dispatch.asynch.AwsSnsNotification
import net.kotlinx.aws.lambda.dispatch.asynch.AwsSnsUnknown
import net.kotlinx.reflect.name
import net.kotlinx.slack.SlackMessageSenders
import net.kotlinx.string.abbr

/** SNS 이벤트 */
class AwsSnsListener {

    private val log = KotlinLogging.logger {}

    companion object {
        /** body에 들어갈 최대 글 수 */
        const val BODY_LIMIT = 300
    }

    //==================================================== SNS들 ======================================================

    @Subscribe
    fun onEvent(event: AwsSnsNotification) {
        SlackMessageSenders.Alert.send {
            workDiv = AwsSnsNotification::class.name()
            descriptions = listOf(event.subject)
            body = listOf(event.message.abbr(BODY_LIMIT))
        }
    }

    @Subscribe
    fun onEvent(event: AwsSnsAlarm) {
        SlackMessageSenders.Alert.send {
            workDiv = AwsSnsAlarm::class.name()
            descriptions = listOf("SNS 알림! ${event.alarmName}")
            body = listOf(event.data.toPreety().abbr(BODY_LIMIT))
        }
    }

    @Subscribe
    fun onEvent(event: AwsSnsUnknown) {
        SlackMessageSenders.Alert.send {
            workDiv = AwsSnsUnknown::class.name()
            descriptions = listOf("알 수 없는 SNS 전달입니다. 파싱해주세요")
            body = listOf(event.body.toPreety().abbr(BODY_LIMIT))
        }
    }
}