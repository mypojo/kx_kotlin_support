package net.kotlinx.slack

import net.kotlinx.aws.AwsClient1
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.gson.GsonSet
import net.kotlinx.test.TestLight
import org.koin.core.component.get


class SlackApp_응답봇Test : TestLight() {

    val token = get<AwsClient1>().ssmStore["/slack/token"]!!
    val app = SlackApp(token)

    fun `스래드 수신`(): String {

        val slackInput = GsonData.obj()
        slackInput["challenge"].lett {
            log.warn { "슬랙 연결 인증 수신.." }
            return slackInput.toString()
        }

        log.info { "슬랙 메세지" }
        println(GsonSet.GSON_PRETTY.toJson(slackInput.delegate))

        val token = "xx"
        val slackApp = SlackApp(token)

        slackInput["event"].lett { event ->
            when (event["type"].str!!) {
                "reaction_added" -> {
                    val reaction = event["reaction"].str!!

                    val item = event["item"]
                    val ts = item["ts"].str!!
                    val channel = item["channel"].str!!
                    //slackApp.chatPostMessageReply(channel, ts, "reaction_added $reaction")
                }

                "reaction_removed" -> {
                    val reaction = event["reaction"].str!!

                    val item = event["item"]
                    val ts = item["ts"].str!!
                    val channel = item["channel"].str!!
                    //slackApp.chatPostMessageReply(channel, ts, "reaction_removed $reaction")
                }

                "app_mention" -> {
                    val ts = event["ts"].str!!
                    val channel = event["channel"].str!!
                    val text = event["text"].str!!
                    //slackApp.chatPostMessageReply(channel, ts, "응답 $text")
                }

                else -> {
                    println("??????????")
                }
            }
        }
        return "ok"
    }

}