package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockagentruntime.BedrockAgentRuntimeClient
import aws.sdk.kotlin.services.bedrockagentruntime.model.InvokeAgentResponse
import aws.sdk.kotlin.services.bedrockagentruntime.model.ResponseStream
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

/** 배드락 에이전트 추론 관련 클라이언트 */
val AwsClient.brar: BedrockAgentRuntimeClient
    get() = getOrCreateClient { BedrockAgentRuntimeClient { awsConfig.build(this) }.regist(awsConfig) }

/**
 * 배드락 응답 결과 간단 파싱
 * 1. 결과는 문자로 리턴  ( 대용량 응답이 아니라고 가정 )
 * 2. 트레이스는 콜백으로 처리
 * */
suspend fun InvokeAgentResponse.toSimpleText(debug: (Int, ResponseStream.Trace) -> Unit = { _, _ -> }): String {
    val list = mutableListOf<String>()
    var traceCnt = 0
    this.completion!!.collect { event ->
        when (event) {
            is ResponseStream.Chunk -> {
                val result = event.value.bytes?.decodeToString() ?: "" //일반적으로 Chunk 로 리턴됨
                list.add(result)
            }

            is ResponseStream.Trace -> {
                debug.invoke(traceCnt++, event)
            }

            else -> throw IllegalArgumentException("지원하지 않는 타입 : ${event::class.qualifiedName}")
        }
    }
    return list.joinToString()
}
