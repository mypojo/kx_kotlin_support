package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockruntime.converse
import aws.sdk.kotlin.services.bedrockruntime.model.ContentBlock
import aws.sdk.kotlin.services.bedrockruntime.model.ConversationRole
import aws.sdk.kotlin.services.bedrockruntime.model.Message
import aws.sdk.kotlin.services.bedrockruntime.model.SystemContentBlock
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class BedrockRuntimeSupportTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("베드락 런타임 테스트") {

            Then("로우 API - 텍스트멀티") {
                val resp = aws97.brr.converse {
                    // BedrockRuntimeClient
                    this.modelId = BedrockModels.OnDemand.CLAUDE_3_HAIKU.id
                    this.system = listOf(
                        SystemContentBlock.Text("샘플 따라해봐")
                    )
                    this.messages = listOf(
                        Message {
                            role = ConversationRole.User
                            content = listOf(
                                ContentBlock.Text("반지의제왕")
                            )
                        },
                        Message {
                            role = ConversationRole.Assistant
                            content = listOf(
                                ContentBlock.Text(
                                    """
                                        #1
                                        반지의 제왕: 왕의 귀환
                                        개봉연도 : 2003
                                        감독 : 피터 잭슨
                                        #2
                                        반지의 제왕: 두 개의 탑
                                        개봉연도 : 2002
                                        감독 : 피터 잭슨
                                    """.trimIndent()
                                )
                            )
                        },
                        Message {
                            role = ConversationRole.User
                            content = listOf(
                                ContentBlock.Text("다이하드")
                            )
                        },
                    )
                }
                println(resp.output?.asMessage())
                println(resp.usage)
            }

        }
    }

}
