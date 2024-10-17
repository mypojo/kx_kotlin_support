package net.kotlinx.slack

import aws.sdk.kotlin.services.codedeploy.model.LifecycleEventStatus
import net.kotlinx.aws.lambda.dispatch.asynch.CodeDeployHookEvent
import net.kotlinx.domain.developer.DeveloperData
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.okhttp.build
import net.kotlinx.reflect.name
import net.kotlinx.slack.msg.SlackSimpleAlert
import okhttp3.HttpUrl.Companion.toHttpUrl

class SlackApp_메세지템플릿 : BeSpecHeavy() {

    init {
        initTest(KotestUtil.SLOW)

        Given("SlackSimpleAlert") {

            val hookEvent = CodeDeployHookEvent("aaa", "bbb")

            Then("코드디플로이 훅 승인메세지 데모") {
                val host = "https://naver.com"
                val hostTest = "http://naver.com:8080"
                val testLink = "테스트하러가기".slackLink(hostTest)
                val hostApiPath = "${host}/api/system/codedeploy/event"

                val pairs = listOf(
                    "승인" to LifecycleEventStatus.Succeeded,
                    "반려" to LifecycleEventStatus.Failed,
                )
                val buttonLink = pairs.joinToString(" / ") {
                    val link = hostApiPath.toHttpUrl().build {
                        addQueryParameter(CodeDeployHookEvent::deploymentId.name, hookEvent.deploymentId)
                        addQueryParameter(CodeDeployHookEvent::lifecycleEventHookExecutionId.name, hookEvent.lifecycleEventHookExecutionId)
                        addQueryParameter(LifecycleEventStatus::class.name(), it.second.value)
                    }
                    it.first.slackLink(link)
                }
                SlackMessageSenders.Alert.send {
                    workDiv = "코드디플로이 승인/반려"
                    descriptions = listOf("$testLink -> $buttonLink")
                }
            }

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