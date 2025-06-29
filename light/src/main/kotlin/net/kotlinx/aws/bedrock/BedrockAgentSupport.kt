package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockagent.*
import aws.sdk.kotlin.services.bedrockagent.model.ApiSchema
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

/**
 * 액션 그룹의 오픈 API 스키마를 업데이트함
 * 빌드시 사용됨
 * */
suspend fun BedrockAgentClient.updateAgentActionGroupSchema(agentId: String, actionGroupId: String, schema: String) {

    val exist = this.getAgentActionGroup {
        this.agentId = agentId
        this.agentVersion = "DRAFT"  //일단 최신만
        this.actionGroupId = actionGroupId
    }.agentActionGroup!!

    this.updateAgentActionGroup {
        this.agentId = agentId
        this.agentVersion = "DRAFT"
        this.actionGroupId = actionGroupId

        this.actionGroupName = exist.actionGroupName
        this.description = exist.description

        // OpenAPI 스키마 설정
        this.apiSchema = ApiSchema.Payload(schema)

        // 기존 설정 유지
        this.actionGroupExecutor = exist.actionGroupExecutor
        this.actionGroupState = exist.actionGroupState
    }

}
