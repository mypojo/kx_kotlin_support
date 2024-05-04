package net.kotlinx.slack.template

import net.kotlinx.core.dev.DeveloperData
import net.kotlinx.kotest.BeSpecLight
import org.junit.jupiter.api.Test

class SlackSimpleAlertTest : BeSpecLight() {

    @Test
    fun `성공메세지`() {

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
        alert.send()

    }


    @Test
    fun test() {

        val alert = SlackSimpleAlert {
            source = "demo"
            workDiv = "test"
            mainMsg = "??"
        }
        alert.send()

    }

}
