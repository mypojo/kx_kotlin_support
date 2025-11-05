package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockagent.BedrockAgentClient
import aws.sdk.kotlin.services.bedrockagent.getAgent
import aws.sdk.kotlin.services.bedrockagent.model.CreationMode
import aws.sdk.kotlin.services.bedrockagent.model.PromptOverrideConfiguration
import aws.sdk.kotlin.services.bedrockagent.model.PromptType
import aws.sdk.kotlin.services.bedrockagent.prepareAgent
import aws.sdk.kotlin.services.bedrockagent.updateAgent
import mu.KotlinLogging
import net.kotlinx.string.toTextGridPrint

/**
 * 에이전트 프롬프트 업데이트 (여러 타입을 한 번에 교체 가능)
 * Orchestration : 일반적인 프롬프트
 */
suspend fun BedrockAgentClient.updatePrompt(agentId: String, block: () -> Map<PromptType, String>) {

    val promptMap = block()

    // 기존 에이전트 정보 조회
    val agent = this.getAgent {
        this.agentId = agentId
    }.agent ?: throw IllegalStateException("Agent not found: $agentId")

    val updatedOverride = buildUpdatedPromptOverride(agent.promptOverrideConfiguration, promptMap)

    // 에이전트 업데이트 (기존 필드 유지)
    this.updateAgent {
        this.agentId = agentId
        this.agentName = agent.agentName
        this.agentResourceRoleArn = agent.agentResourceRoleArn
        this.foundationModel = agent.foundationModel
        this.instruction = agent.instruction
        this.idleSessionTtlInSeconds = agent.idleSessionTtlInSeconds
        this.description = agent.description
        this.promptOverrideConfiguration = updatedOverride
        this.agentCollaboration = agent.agentCollaboration

        //불명확한것들
        this.customerEncryptionKeyArn = agent.customerEncryptionKeyArn
        this.guardrailConfiguration = agent.guardrailConfiguration
        this.memoryConfiguration = agent.memoryConfiguration
    }
    // 변경사항 적용을 위해 prepare 수행
    this.prepareAgent { this.agentId = agentId }
}

private fun buildUpdatedPromptOverride(existing: PromptOverrideConfiguration?, newPrompts: Map<PromptType, String>): PromptOverrideConfiguration {
    val exists = existing!!.promptConfigurations

    val log = KotlinLogging.logger {}
    if (log.isDebugEnabled) {
        listOf("promptType", "promptCreationMode", "promptState").toTextGridPrint {
            exists.map { arrayOf(it.promptType, it.promptCreationMode, it.promptState) }
        }
    }

    // 기존 설정을 보존하면서 전달된 타입만 basePromptTemplate 교체
    val newList = exists
        .filter { it.promptCreationMode == CreationMode.Overridden } //it.promptState == PromptState.Enabled &&
        .map { exist ->
            val input = newPrompts[exist.promptType]
            if (input == null) {
                exist
            } else {
                exist.copy { basePromptTemplate = input }
            }
        }

    if (log.isDebugEnabled) {
        listOf("promptType", "promptCreationMode", "promptState").toTextGridPrint {
            newList.map { arrayOf(it.promptType, it.promptCreationMode, it.promptState) }
        }
    }

    return PromptOverrideConfiguration {
        //주의!! 여기 DEFAULT 가 아닌 변경분만 넣어야 한다!! 아니면 에러남! 이게 API마다 규칙이 다른듯
        promptConfigurations = newList
    }
}