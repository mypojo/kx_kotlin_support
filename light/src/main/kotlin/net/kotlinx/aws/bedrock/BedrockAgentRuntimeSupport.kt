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
 * 간단 텍스트 변환
 * 수정의 여지 있음
 *  */
suspend fun InvokeAgentResponse.toSimpleText(): String {
    val datas = mutableListOf<String>()
    this.completion!!.collect { event ->
        val append = when (event) {
            is ResponseStream.Chunk -> event.value.bytes?.decodeToString() ?: ""
            is ResponseStream.Trace -> event.value.toString()

            else -> throw IllegalArgumentException("지원하지 않는 타입 : ${event::class.qualifiedName}")
        }
        datas.add(append)
    }
    return datas.joinToString()
}