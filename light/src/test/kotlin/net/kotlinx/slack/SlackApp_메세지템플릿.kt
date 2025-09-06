package net.kotlinx.slack

import aws.sdk.kotlin.services.codedeploy.model.LifecycleEventStatus
import net.kotlinx.aws.lambda.dispatch.asynch.AwsCodeDeployHookEvent
import net.kotlinx.domain.developer.DeveloperData
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.okhttp.build
import net.kotlinx.reflect.name
import net.kotlinx.slack.msg.SlackSimpleAlert
import net.kotlinx.string.toTextGrid
import okhttp3.HttpUrl.Companion.toHttpUrl

class SlackApp_메세지템플릿 : BeSpecHeavy() {

    init {
        initTest(KotestUtil.SLOW)

        Given("SlackSimpleAlert") {

            val hookEvent = AwsCodeDeployHookEvent("aaa", "bbb")

            xThen("코드디플로이 훅 승인메세지 데모") {
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
                        addQueryParameter(AwsCodeDeployHookEvent::deploymentId.name, hookEvent.deploymentId)
                        addQueryParameter(AwsCodeDeployHookEvent::lifecycleEventHookExecutionId.name, hookEvent.lifecycleEventHookExecutionId)
                        addQueryParameter(LifecycleEventStatus::class.name(), it.second.value)
                    }
                    it.first.slackLink(link)
                }
                SlackMessageSenders.Alert.send {
                    workDiv = "코드디플로이 승인/반려"
                    descriptions = listOf("$testLink -> $buttonLink")
                }
            }

            xThen("에러메세지 데모 - 개인메세지") {
                SlackMessageSenders.Alert.send {
                    descriptions += listOf("추가메세지 2 ${"here".slackMention("!")}")
                }
//                SlackMessageSenders.Alert.send {
//                    toUser = "@U0641U84CUE"
//                    descriptions += listOf("추가메세지")
//                }
            }

            Then("성공메세지 데모") {

                val lines = listOf(
                    arrayOf("1", "2025년09월02일(화) 14시50분55초", "dskim", "Merge remote-tracking branch 'origin/master'"),
                    arrayOf("2", "2025년09월02일(화) 14시50분39초", "dskim", "SQS 재시도 로직 분리 및 테스트 수정- `sendSqs` 메서드 분리로 SQS 처리 로직 가독성 개선- `NvMasterDemoJobT.."),
                    arrayOf("3", "2025년09월02일(화) 14시34분33초", "sin", "권한 디버깅"),
                    arrayOf("4", "2025년09월02일(화) 14시06분51초", "sin", "권한 디버깅"),
                )
                val grid = listOf("sha", "Date", "Author", "Message").toTextGrid(lines)

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
                        grid.text,
                    )
                }
                slackApp.send(alert)
            }
        }

    }
}