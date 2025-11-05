package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockagentruntime.BedrockAgentRuntimeClient
import aws.sdk.kotlin.services.bedrockagentruntime.model.InvokeAgentRequest
import aws.sdk.kotlin.services.bedrockagentruntime.model.SessionState
import kotlinx.coroutines.flow.toList
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.MyEnv
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print
import net.kotlinx.system.DeploymentType
import java.util.*


data class TextToSqlResult(
    val success: Boolean,
    val sql: String,
    val errorMessage: String
)

suspend fun BedrockAgentRuntimeClient.invokeAgentTextToSql(text: String): TextToSqlResult {
    val agentInfo = when (MyEnv.DEPLOYMENT_TYPE) {
        DeploymentType.PROD -> throw UnsupportedOperationException("미지원")
        DeploymentType.DEV -> "G0MX1ZA4DQ" to if (MyEnv.IS_LOCAL) BedrockAgentUtil.LATEST else "??"
    }
    val invokeRequest = InvokeAgentRequest {
        this.agentId = agentInfo.first
        this.agentAliasId = agentInfo.second
        this.sessionId = UUID.randomUUID().toString()  //단답형임
        this.inputText = "2025년 11월 1일~4일간 네이버 광고주별 노클비와 외부유입 클릭수를 산출해줘"
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
    val resultTotalText = this.invokeAgent(invokeRequest) { it.toSimpleText() }
    return resultTotalText.toGsonData().fromJson<TextToSqlResult>()
}

class BedrockAgentSupportTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("베드락 에이전트") {

            Then("에이전트 실행") {
                val result = aws49.brar.invokeAgentTextToSql("2025년 11월 1일~4일간 네이버 광고주별 노클비와 외부유입 클릭수를 산출해줘")
                println(result)

            }

            Then("프롬프트 리스팅") {
                val resp = aws49.bra.listAllPrompts()
                resp.toList().print()
            }

            Then("프롬프트 가져오기") {
                val resp = aws49.bra.getPrompt("WCMF5L3D5Z")
                listOf(resp).print()
            }


        }
    }

}
