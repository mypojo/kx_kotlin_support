package net.kotlinx.slack

import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.gson.GsonSet
import net.kotlinx.core.test.TestRoot
import org.junit.jupiter.api.Test

class SlackAppTest : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient()


    @Test
    fun test() {

        val token = aws.ssmStore["/slack/nov/token"]!!
        val app = SlackApp(token)
        app.chatPostMessage("U037LL28D4P", "aaa")


    }

    fun `슬랙봇 샘플`(): String {

        val slackInput = GsonData.obj()
        slackInput["challenge"].lett {
            log.warn { "슬랙 연결 인증 수신.." }
            return slackInput.toString()
        }

        log.info { "슬랙 메세지" }
        println(GsonSet.GSON_PRETTY.toJson(slackInput.delegate))

        val token = aws.ssmStore["/slack/nov/token"]!!
        val slackApp = SlackApp(token)

        slackInput["event"].lett { event ->
            when (event["type"].str!!) {
                "reaction_added" -> {
                    val reaction = event["reaction"].str!!

                    val item = event["item"]
                    val ts = item["ts"].str!!
                    val channel = item["channel"].str!!
                    slackApp.chatPostMessageReply(channel, ts, "reaction_added $reaction")
                }

                "reaction_removed" -> {
                    val reaction = event["reaction"].str!!

                    val item = event["item"]
                    val ts = item["ts"].str!!
                    val channel = item["channel"].str!!
                    slackApp.chatPostMessageReply(channel, ts, "reaction_removed $reaction")
                }

                "app_mention" -> {
                    val ts = event["ts"].str!!
                    val channel = event["channel"].str!!
                    val text = event["text"].str!!
                    slackApp.chatPostMessageReply(channel, ts, "응답 $text")
                }

                else -> {
                    println("??????????")
                }
            }
        }
        return "ok"
    }

}