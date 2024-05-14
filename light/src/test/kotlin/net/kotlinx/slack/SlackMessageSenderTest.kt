package net.kotlinx.slack

import mu.KotlinLogging
import net.kotlinx.domain.developer.DeveloperData
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.MyEnv
import net.kotlinx.slack.msg.SlackSimpleAlert

object SlackMessageSenderSet {

    private val log = KotlinLogging.logger {}

    val ON_ERROR = object : SlackMessageSender {

        private val slackApp = koin<SlackApp>()

        override fun send(block: SlackSimpleAlert.() -> Unit) {

            if (MyEnv.IS_LOCAL) {
                log.warn { "로컬 전송입니다!" }
            }

            val alert = SlackSimpleAlert {
                channel = "#kx_alert"
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
            }
            slackApp.send(alert)
        }

    }


}