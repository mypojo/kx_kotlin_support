package net.kotlinx.slack

import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.gson.GsonSet
import net.kotlinx.kotest.BeSpecLight
import org.junit.jupiter.api.Test


class SlackAppTest : BeSpecLight() {

    @Test
    fun 이미지전송() {
//        val token = get<AwsClient1>().ssmStore["/slack/token"]!!
//        val app = SlackApp(token)
//        app.chatPostMessage {
//            channel("#kx_alert")
//            text("이미지 테스트")
//            attachments(
//                listOf(
//                    Attachment().apply {
//                        this.text = "샘플"
//                        this.fallback = "샘플 이미지"
//                        this.imageUrl = "https://img.freepik.com/premium-vector/online-shop-ads-banner-template_653829-11.jpg?w=1380"
//                        this.thumbUrl = "https://img.freepik.com/premium-vector/online-shop-ads-banner-template_653829-11.jpg?w=1380"
//                    }
//                )
//            )
//        }
    }

    fun `스래드 전송`(): String {

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