package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockagentruntime.model.InvokeAgentRequest
import aws.sdk.kotlin.services.bedrockagentruntime.model.SessionState
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print
import java.util.*


class BedrockAgentSupportTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("베드락 에이전트") {

            Then("에이전트 실행") {

                val sessionId = UUID.randomUUID().toString()

                val invokeRequest = InvokeAgentRequest {
                    this.agentId = "7V7ABTRDTY"
                    this.agentAliasId = "86VL3UVUZM"
                    this.sessionId = sessionId
                    this.inputText = "하루 800원 쓸건데 , 지금 남은 광고비와, 광고비 날짜별로 얼마 남을지 표로 정리해줘."
                    this.sessionState = SessionState {
                        this.sessionAttributes = mapOf(
                            "JWT" to "aaa",
                            "USER_ID" to "bbb",
                        )
                        this.promptSessionAttributes = mapOf(
                            "JWT" to "aaa",
                        )
                    }
                }

                //event: {'messageVersion': '1.0', 'inputText': '지금 남은 광고비가 얼마지?', 'sessionAttributes': {'USER_ID': 'bbb', 'JWT': 'aaa'}, 'promptSessionAttributes': {'JWT': 'aaa'}, 'sessionId': '8e67f8f0-c1a9-485e-b434-635c63b81eda', 'agent': {'name': 'dmp-mcp-demo', 'version': '1', 'id': 'L8YEZD07X9', 'alias': 'MYEVDEKRV4'}, 'actionGroup': 'naver_api', 'httpMethod': 'GET', 'apiPath': '/bizMoney'}

                val resultTotalText = aws49.brar.invokeAgent(invokeRequest) { it.toSimpleText() }
                println(resultTotalText)

            }

            Then("프롬프트 리스팅") {
                val resp = aws49.bra.listAllPrompts()
                resp.print()
            }

            Then("프롬프트 가져오기") {
                val resp = aws49.bra.getPrompt("WCMF5L3D5Z")
                listOf(resp).print()
            }


        }
    }

}
