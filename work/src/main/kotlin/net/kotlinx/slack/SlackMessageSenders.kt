package net.kotlinx.slack

import mu.KotlinLogging
import net.kotlinx.aws.AwsInstanceMetadata
import net.kotlinx.aws.AwsInstanceType
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.domain.developer.DeveloperData
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.slack.msg.SlackSimpleAlert


sealed class SlackMessageSenders : SlackMessageSender {

    private val log = KotlinLogging.logger {}

    private val slack by koinLazy<SlackApp>()
    private val instanceMetadata by koinLazy<AwsInstanceMetadata>()

    protected abstract val channels: List<String>

    /** 공통 템플릿 예제 */
    override fun send(block: SlackSimpleAlert.() -> Unit) {

        val instanceType = AwsInstanceTypeUtil.INSTANCE_TYPE
        if (instanceType == AwsInstanceType.LOCAL) {
            log.warn { "[${this}] local임으로 로그를 무시합니다. 채널 : ${channels}}" }
            //return
        }

        channels.forEach { targetChannel ->
            val alert = SlackSimpleAlert {
                channel = targetChannel
                source = "kx_project-dev"
                workDiv = "sync_meta01"
                workDivLink = "https://www.google.co.kr/"
                workLocation = "LAMBDA"
                workLocationLink = "https://naver.com"
                developers = listOf(DeveloperData(id = "xx", slackId = "U0641U84CUE"))
                exception = Exception("예외발생")
                descriptions = listOf(
                    "너무 많은 예산이 소진되었습니다.",
                    "예산을 높여주세요",
                )
                body = listOf(
                    "XxxException ... ",
                    " -> xxxxxx",
                )
                block()
            }
            slack.send(alert)
        }

    }

    //==================================================== 실제 메세지들 ======================================================

    /** 단순 경고 메세지 */
    data object Alert : SlackMessageSenders() {
        override val channels = listOf("#kx_alert")
    }


}