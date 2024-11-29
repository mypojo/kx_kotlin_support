package net.kotlinx.openAi.kwdExtract

import com.aallam.openai.api.BetaOpenAI
import mu.KotlinLogging
import net.kotlinx.json.gson.GsonData
import net.kotlinx.openAi.OpenAiClient
import net.kotlinx.openAi.toListString
import net.kotlinx.retry.RetryTemplate

private val CHAT_RETRY = RetryTemplate {
    retries = 6
}

@OptIn(BetaOpenAI::class)
suspend fun OpenAiClient.chatAndExtract(ins: List<KwdExtractIn>): List<KwdExtractOut> {
    return CHAT_RETRY.withRetry {
        val query = ins.joinToString("\n`")
        val completion = this.chat(query)
        val data = GsonData.Companion.parse(completion.toListString().first())
        val outs = KwdExtractOut.fromJson(data)
        try {
            KwdExtractUtil.validate(ins, outs)
        } catch (e: Exception) {
            val log = KotlinLogging.logger {}
            log.warn { "AI[${this.modelId}] 에러! -> 원본 메세지 : ${data}" }
            throw e
        }
        outs
    }
}
