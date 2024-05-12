package net.kotlinx.slack

import net.kotlinx.domain.developer.DeveloperData
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.slack.msg.SlackSimpleAlert

class SlackApp_메세지템플릿 : BeSpecLight() {

    init {
        initTest(KotestUtil.SLOW)

        Given("SlackSimpleAlert") {
            val slackApp = koin<SlackApp>()
            xThen("에러메세지 데모") {
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

            xThen("성공메세지 데모") {
                val alert = SlackSimpleAlert {
                    channel = "#kx_alert"
                    source = "demo_project"
                    workDiv = "test job"
                    workDivLink = "https://www.naver.com"
                    workLocation = "local"
                    workLocationLink = "https://www.naver.com"
                    mainMsg = ":ok: [$source] $workDiv 작업 처리 완료"
                    developers = listOf(DeveloperData("sin", slackId = "U0641U84CUE"))
                    descriptions = listOf(
                        "작업 xx 처리완료",
                    )
                    body = listOf(
                        "처리건수 xx",
                        "처리시간 xx",
                    )
                }
                slackApp.send(alert)
            }
        }

    }
}