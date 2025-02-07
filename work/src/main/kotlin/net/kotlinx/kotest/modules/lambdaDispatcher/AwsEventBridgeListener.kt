package net.kotlinx.kotest.modules.lambdaDispatcher

import com.google.common.eventbus.Subscribe
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.lambda.dispatch.asynch.*
import net.kotlinx.domain.job.EventBridgeJobStatus
import net.kotlinx.domain.job.define.JobDefinitionRepository
import net.kotlinx.reflect.name
import net.kotlinx.slack.SlackMessageSenders
import net.kotlinx.string.abbr

/** AWS 이벤트들 */
class AwsEventBridgeListener {

    private val log = KotlinLogging.logger {}

    companion object {
        /** body에 들어갈 최대 글 수 */
        const val BODY_LIMIT = 300
    }

    @Subscribe
    fun onEvent(event: EventBridgeSchedulerEvent) {
        runBlocking {
            val jobParam = JobDefinitionRepository.findById(event.scheduleName).toJobOption().exe()
            log.info { "잡(${jobParam}) 실행완료" }
        }
    }

    @Subscribe
    fun onEvent(event: EventBridgeEcsTaskStateChange) {
        SlackMessageSenders.Alert.send {
            workDiv = EventBridgeEcsTaskStateChange::class.name()
            descriptions = listOf("ECS 상태변경 ${event.group}")
            body = listOf(event.stoppedReason)
        }
    }

    @Subscribe
    fun onEvent(event: EventBridgeSfnStatus) {
        SlackMessageSenders.Alert.send {
            workDiv = EventBridgeSfnStatus::class.name()
            descriptions = listOf(event.sfnName)
            body = listOf(event.cause.abbr(AwsSnsListener.BODY_LIMIT))
        }
    }

    @Subscribe
    fun onEvent(event: EventBridgePipeline) {
        SlackMessageSenders.Alert.send {
            workDiv = EventBridgePipeline::class.name()
            descriptions = listOf("빌드 ${event.state}")
            body = listOf(event.pipeline)
        }
    }

    @Subscribe
    fun onEvent(event: EventBridgeS3) {
        log.debug { " -> 메시지 전송.. $event" }
        //코드커밋 라이브러리 없어서 못보냄..
        SlackMessageSenders.Alert.send {
            workDiv = EventBridgeS3::class.name()
            descriptions = listOf(
                event.bucket,
                event.key,
                event.reason
            )
            body = emptyList()
            exception = null
        }
    }

    @Subscribe
    fun onEvent(event: EventBridgeJson) {
        log.debug { " -> 메시지 전송.. $event" }
        //코드커밋 라이브러리 없어서 못보냄..
        SlackMessageSenders.Alert.send {
            workDiv = EventBridgeJson::class.name()
            descriptions = listOf("알 수 없는 이벤트브릿지 전달입니다. 파싱해주세요")
            body = listOf(event.body.toPreety()) //알수 없는 메세지는 요약하지 않음
        }
    }

    //==================================================== 이하 각 커스텀 로직에 위치해야할 이벤트 처리 ======================================================

    /** 잡 상태변경 */
    @Subscribe
    fun onStatueChange(event: EventBridgeJobStatus) {
        val job = event.job
        log.warn { "잡 이벤트 변경!! [${job.toKeyString()}] => ${job.jobStatus}" }
    }

}