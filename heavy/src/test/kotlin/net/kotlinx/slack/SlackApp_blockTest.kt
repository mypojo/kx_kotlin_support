package net.kotlinx.slack

import com.slack.api.model.kotlin_extension.block.element.ButtonStyle
import com.slack.api.model.kotlin_extension.block.withBlocks
import net.kotlinx.core.dev.DeveloperData
import net.kotlinx.koin.Koins
import net.kotlinx.kotest.BeSpecLight
import net.kotlinx.slack.template.SlackSimpleAlert
import org.junit.jupiter.api.Test


class SlackApp_blockTest : BeSpecLight() {

    val catImage = "https://pbs.twimg.com/profile_images/625633822235693056/lNGUneLX_400x400.jpg"

    @Test
    fun `에러메세지 데모`() {

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

        alert.workDiv += "??"

        alert.send()

    }

    @Test
    fun `블록 데모`() {

        Koins.koin<SlackApp>().chatPostMessage {
            val mainMsg = ":warning::warning: 고양이가 도망쳤습니다 !!"
            channel("#kx_alert")
            text(mainMsg) //우하단 알럿 메세지에 이게 표시됨

            val blocks = withBlocks {
                //큰글씨. 버튼이나 이미지를 악세서리로 달 수 있음
                section {
                    plainText(mainMsg)
                }
                //작은글씨. 글자와 이미지(이모지x)를 나열 가능
                context {
                    elements {
                        plainText(mainMsg, true)
                        image(catImage, "cute cat")
                        image(catImage, "cute cat")
                    }
                }

                actions {
                    button {
                        text("승인", true)
                        url("https://naver.com")
                        style(ButtonStyle.PRIMARY)

                    }
                }
            }
            this.blocks(blocks)
        }
    }

    @Test
    fun `블록 스래드 데모`() {

        val app = Koins.koin<SlackApp>()
        val threadTs = app.chatPostMessage {
            val msg = ":ok: 요청하신 작업이 종료되었습니다."
            channel("#kx_alert")
            text(msg) //우하단 알럿 메세지에 이게 표시됨

            val blocks = withBlocks {
                section {
                    markdownText(msg)
                }
                section {
                    val text = "입력 이미지"
                    markdownText(text)
                    accessory {
                        //오른쪽 끝에 작은 정방형으로 표시된다. 좌우 넓다면 잘려나감
                        this.image("https://img.freepik.com/premium-vector/online-shop-ads-banner-template_653829-11.jpg?w=1380", text)
                    }
                }

                divider()

                image {
                    imageUrl("https://img.freepik.com/premium-vector/online-shop-ads-banner-template_653829-11.jpg?w=1380")
                    altText("")
                    title("결과1 이미지", false)
                }

                actions {
                    button {
                        text("승인", true)
                        value("aa")
                        style(ButtonStyle.PRIMARY)
                        actionId("action_approve")
                    }
                    button {
                        text("반려", true)
                        value("bb")
                        style(ButtonStyle.DANGER)
                        actionId("action_reject")
                    }
                }
            }
            this.blocks(blocks)
        }

        app.chatPostMessage {
            channel("#kx_alert")
            text("상세 내용")
            threadTs(threadTs)

            val blocks = withBlocks {

                section {
                    markdownText("결과111입니다..")
                }
                image {
                    imageUrl("https://img.freepik.com/premium-vector/online-shop-ads-banner-template_653829-11.jpg?w=1380")
                    altText("")
                    title("결과1 이미지", false)
                }
                divider()
                section {
                    markdownText("결과222입니다..")
                }
                image {
                    imageUrl("https://img.freepik.com/premium-vector/online-shop-ads-banner-template_653829-11.jpg?w=1380")
                    altText("")
                    title("결과2 이미지", false)
                }
            }
            this.blocks(blocks)
        }
    }


}