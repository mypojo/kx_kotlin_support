package net.kotlinx.slack

import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.GsonSet
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class SlackApp_양방향봇 : BeSpecHeavy() {

    init {
        initTest(KotestUtil.FAST)

        Given("SlackApp_양방향봇") {

            //val slackApp by lazyKoin<SlackApp>()

            xThen("봇으로 전달된 슬렉 메시지를 서버가 수신해서 응답 해준다") {
                val slackInput = GsonData.obj()
                slackInput["challenge"].lett {
                    log.warn { "슬랙 연결 인증 수신.." }
                }

                log.info { "슬랙 메세지" }
                println(GsonSet.GSON_PRETTY.toJson(slackInput.delegate))

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
            }
        }
    }


}