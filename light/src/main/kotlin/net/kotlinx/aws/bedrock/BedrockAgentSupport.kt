package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockagent.BedrockAgentClient
import aws.sdk.kotlin.services.bedrockagent.getAgentActionGroup
import aws.sdk.kotlin.services.bedrockagent.getPrompt
import aws.sdk.kotlin.services.bedrockagent.model.ApiSchema
import aws.sdk.kotlin.services.bedrockagent.model.GetPromptResponse
import aws.sdk.kotlin.services.bedrockagent.model.PromptSummary
import aws.sdk.kotlin.services.bedrockagent.paginators.listPromptsPaginated
import aws.sdk.kotlin.services.bedrockagent.updateAgentActionGroup
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

/** 배드락 에이전트 클라이언트 */
val AwsClient.bra: BedrockAgentClient
    get() = getOrCreateClient { BedrockAgentClient { awsConfig.build(this) }.regist(awsConfig) }

private val log = KotlinLogging.logger {}

/** 프롬프트 가져오기.. 실제 텍스트는 못가져옴 */
suspend fun BedrockAgentClient.getPrompt(promptId: String, version: String? = null): GetPromptResponse {
    return getPrompt {
        this.promptIdentifier = promptId
        this.promptVersion = version
    }
}

/** 전체 프롬프트 출력 -> Flow 지원 */
fun BedrockAgentClient.listAllPrompts(): kotlinx.coroutines.flow.Flow<PromptSummary> = listPromptsPaginated {}.flatMapConcat { it.promptSummaries.asFlow() }

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