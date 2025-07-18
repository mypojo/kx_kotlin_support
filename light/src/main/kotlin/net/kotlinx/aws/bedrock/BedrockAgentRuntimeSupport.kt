package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockagentruntime.BedrockAgentRuntimeClient
import aws.sdk.kotlin.services.bedrockagentruntime.model.InvokeAgentResponse
import aws.sdk.kotlin.services.bedrockagentruntime.model.ResponseStream
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

/** 배드락 에이전트 추론 관련 클라이언트 */
val AwsClient.brar: BedrockAgentRuntimeClient
    get() = getOrCreateClient { BedrockAgentRuntimeClient { awsConfig.build(this) }.regist(awsConfig) }


/** 간단 텍스트 변환 */
suspend fun InvokeAgentResponse.toSimpleText(): String = buildString { toSimpleText { append(it) } }

/** 간단 텍스트 변환 - 스트리밍 */
suspend fun InvokeAgentResponse.toSimpleText(block: (String) -> Unit = {}) {
    this.completion!!.collect { event ->
        val append = when (event) {
            is ResponseStream.Chunk -> event.value.bytes?.decodeToString() ?: "" //일반적으로 Chunk 로 리턴됨
            is ResponseStream.Trace -> event.value.toString()
            else -> throw IllegalArgumentException("지원하지 않는 타입 : ${event::class.qualifiedName}")
        }
        block(append)
    }
}