package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockagent.BedrockAgentClient
import aws.sdk.kotlin.services.bedrockagent.getPrompt
import aws.sdk.kotlin.services.bedrockagent.listPrompts
import aws.sdk.kotlin.services.bedrockagent.model.GetPromptResponse
import aws.sdk.kotlin.services.bedrockagent.model.PromptSummary
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist
import net.kotlinx.collection.doUntilTokenNull

/** 배드락 에이전트 클라이언트 */
val AwsClient.bra: BedrockAgentClient
    get() = getOrCreateClient { BedrockAgentClient { awsConfig.build(this) }.regist(awsConfig) }


/** 프롬프트 가져오기.. 실제 텍스트는 못가져옴 */
suspend fun BedrockAgentClient.getPrompt(promptId: String, version: String? = null): GetPromptResponse {
    return getPrompt {
        this.promptIdentifier = promptId
        this.promptVersion = version
    }
}

/** 전체 프롬프트 출력 */
suspend fun BedrockAgentClient.listAllPrompts(): List<PromptSummary> {
    return doUntilTokenNull { _, token ->
        val response = this.listPrompts {
            this.nextToken = token as String?
        }
        response.promptSummaries to response.nextToken
    }.flatten()
}
