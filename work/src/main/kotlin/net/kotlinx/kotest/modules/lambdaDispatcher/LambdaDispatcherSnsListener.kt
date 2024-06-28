package net.kotlinx.kotest.modules.lambdaDispatcher

import com.google.common.eventbus.Subscribe
import mu.KotlinLogging
import net.kotlinx.aws.lambda.dispatch.asynch.*
import net.kotlinx.reflect.name
import net.kotlinx.slack.SlackMessageSenders
import net.kotlinx.string.abbr

/** SNS 이벤트 */
class LambdaDispatcherSnsListener {

    private val log = KotlinLogging.logger {}

    companion object {
        /** body에 들어갈 최대 글 수 */
        const val BODY_LIMIT = 300
    }

    //==================================================== SNS들 ======================================================

    @Subscribe
    fun onEvent(event: SnsNotification) {
        SlackMessageSenders.Alert.send {
            workDiv = SnsNotification::class.name()
            descriptions = listOf(event.subject)
            body = listOf(event.message.abbr(BODY_LIMIT))
        }
    }

    @Subscribe
    fun onEvent(event: SnsEcsTaskStateChange) {
        SlackMessageSenders.Alert.send {
            workDiv = SnsEcsTaskStateChange::class.name()
            descriptions = listOf("ECS 상태변경 ${event.group}")
            body = listOf(event.stoppedReason)
        }
    }

    @Subscribe
    fun onEvent(event: SnsSfnFail) {
        SlackMessageSenders.Alert.send {
            workDiv = SnsSfnFail::class.name()
            descriptions = listOf(event.jobName)
            body = listOf(event.cause.abbr(BODY_LIMIT))
        }
    }

    @Subscribe
    fun onEvent(event: SnsPipeline) {
        SlackMessageSenders.Alert.send {
            workDiv = SnsPipeline::class.name()
            descriptions = listOf("빌드 ${event.state}")
            body = listOf(event.pipeline)
        }
    }

    @Subscribe
    fun onEvent(event: SnsTrigger) {
        SlackMessageSenders.Alert.send {
            workDiv = SnsTrigger::class.name()
            descriptions = listOf("SNS 알림! ${event.alarmName}")
            body = listOf(event.data.toPreety().abbr(BODY_LIMIT))
        }
    }

    @Subscribe
    fun onEvent(event: SnsUnknown) {
        SlackMessageSenders.Alert.send {
            workDiv = SnsUnknown::class.name()
            descriptions = listOf("알 수 없는 SNS 전달입니다. 파싱해주세요")
            body = listOf(event.data.toPreety().abbr(BODY_LIMIT))
        }
    }
}