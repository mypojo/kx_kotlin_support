package net.kotlinx.kotest.modules.lambdaDispatcher

import com.google.common.eventbus.Subscribe
import mu.KotlinLogging
import net.kotlinx.aws.lambda.dispatch.LambdaDispatcherDeadEvent
import net.kotlinx.aws.lambda.dispatch.LambdaDispatcherFailEvent
import net.kotlinx.reflect.name
import net.kotlinx.slack.SlackMessageSenders
import net.kotlinx.string.abbr

/** 기본 이벤트 */
class LambdaDispatcherDefaultListener {

    private val log = KotlinLogging.logger {}

    companion object {
        /** body에 들어갈 최대 글 수 */
        const val BODY_LIMIT = 300
    }

    @Subscribe
    fun onEvent(event: LambdaDispatcherFailEvent) {
        SlackMessageSenders.Alert.send {
            workDiv = LambdaDispatcherDeadEvent::class.name()
            descriptions = listOf("작업 실패!!")
            body = listOf(event.gsonData.toPreety().abbr(BODY_LIMIT))
            exception = event.e
        }
    }

    @Subscribe
    fun onEvent(event: LambdaDispatcherDeadEvent) {
        SlackMessageSenders.Alert.send {
            workDiv = LambdaDispatcherDeadEvent::class.name()
            descriptions = listOf("데드 메세지 발생!!")
            body = listOf(event.gsonData.toPreety().abbr(BODY_LIMIT))
        }
    }

}