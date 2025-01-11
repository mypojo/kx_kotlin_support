package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockruntime.BedrockRuntimeClient
import aws.sdk.kotlin.services.bedrockruntime.invokeModel
import net.kotlinx.ai.AiModel
import net.kotlinx.ai.AiTextResult
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.ResultGsonData
import net.kotlinx.json.gson.toGsonData

/** 배드락 추론 관련 클라이언트 */
val AwsClient.brr: BedrockRuntimeClient
    get() = getOrCreateClient { BedrockRuntimeClient { awsConfig.build(this) }.regist(awsConfig) }

/**
 * agent 아닌, 일반 모델 실행
 * */
suspend fun BedrockRuntimeClient.invokeModel(model: AiModel, input: Any): AiTextResult {
    val start = System.currentTimeMillis()
    val resp = this.invokeModel {
        this.modelId = model.id
        this.contentType = "application/json" //json으로 통일
        this.accept = "application/json" //json으로 통일
        this.body = input.toString().toByteArray()
    }

    val body = GsonData.parse(resp.body.toString(Charsets.UTF_8))
    val content: GsonData = body["content"]
    val inputTokens = body["usage"]["input_tokens"].int!!
    val outputTokens = body["usage"]["output_tokens"].int!!

    val duration = System.currentTimeMillis() - start
    return try {
        check(content.size == 1)
        val result = content[0]["text"].str?.let { ResultGsonData(true, it.toGsonData()) } ?: ResultGsonData(false, content)
        AiTextResult(model,input, result, inputTokens, outputTokens, duration)
    } catch (e: Exception) {
        val result = ResultGsonData(false, content)
        AiTextResult(model,input, result, inputTokens, outputTokens, duration)
    }
}
