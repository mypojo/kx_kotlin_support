package net.kotlinx.slack

import net.kotlinx.domain.developer.DeveloperData
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.slack.msg.SlackSimpleAlert

class SlackApp_메세지템플릿 : BeSpecHeavy() {

    init {
        initTest(KotestUtil.SLOW)

        Given("SlackSimpleAlert") {

            xThen("에러메세지 데모") {
                SlackMessageSenders.Alert.send {
                    descriptions += listOf("추가메세지")
                }
            }

            xThen("성공메세지 데모") {
                val slackApp by koinLazy<SlackApp>()
                val alert = SlackSimpleAlert {
                    channel = "#kx_alert"
                    source = "demo_project"
                    workDiv = "test job"
                    workDivLink = "https://www.naver.com"
                    workLocation = "batch2"
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