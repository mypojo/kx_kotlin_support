package net.kotlinx.kotest.modules.lambdaDispatcher

import com.google.common.eventbus.Subscribe
import mu.KotlinLogging
import net.kotlinx.aws.lambda.dispatch.LambdaDispatcherDeadEvent
import net.kotlinx.aws.lambda.dispatch.asynch.CodeDeployHookEvent
import net.kotlinx.aws.lambda.dispatch.asynch.SqsEvent
import net.kotlinx.reflect.name
import net.kotlinx.slack.SlackMessageSenders

/** AWS 이벤트들 */
class AwsEventListener {

    private val log = KotlinLogging.logger {}

    companion object {
        /** body에 들어갈 최대 글 수 */
        const val BODY_LIMIT = 300
    }


    @Subscribe
    fun onEvent(event: CodeDeployHookEvent) {
        SlackMessageSenders.Alert.send {
            workDiv = LambdaDispatcherDeadEvent::class.name()
            descriptions = listOf("CodeDeployHookEvent 해주세요!!!")
        }
    }

    @Subscribe
    fun onEvent(event: SqsEvent) {
        SlackMessageSenders.Alert.send {
            workDiv = LambdaDispatcherDeadEvent::class.name()
            descriptions = listOf("SqsEvent 해주세요!!!")
        }
    }

}