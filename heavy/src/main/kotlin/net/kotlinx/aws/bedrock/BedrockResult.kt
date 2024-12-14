package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockruntime.model.InvokeModelResponse
import net.kotlinx.json.gson.GsonData

/**
 * 간단 래퍼
 * */
class BedrockResult(val resp: InvokeModelResponse) {

    /** 결과의 바디 */
    val body: GsonData by lazy { GsonData.parse(resp.body.toString(Charsets.UTF_8)) }

    /** 컨텐츠 그대로 */
    val content: GsonData by lazy { body["content"] }

    /** 결과 데이터. null인경우 프롬프트 오류 */
    val data: GsonData? by lazy {
        check(content.size == 1)
        val json = content[0]["text"].str ?: return@lazy null
        GsonData.parse(json)
    }

    val inputTokens: Int by lazy { body["usage"]["input_tokens"].int!! }

    val outputTokens: Int by lazy { body["usage"]["output_tokens"].int!! }


}