package net.kotlinx.gpt

import com.lectra.koson.obj
import net.kotlinx.core.Kdsl
import net.kotlinx.koin.Koins
import net.kotlinx.okhttp.fetch
import net.kotlinx.props.lazyLoadString
import okhttp3.OkHttpClient

/**
 * 단단한 래퍼
 * https://github.com/aallam/openai-kotlin  참고!!
 *
 * GPT는 요금제가 2가지?
 *  웹 콘솔 = 월 과금인듯
 *  API 호출 = 종량제인듯
 *  */
class GptClient {

    @Kdsl
    constructor(block: GptClient.() -> Unit = {}) {
        apply(block)
    }

    /**
     * API key
     * 키 받는곳 https://platform.openai.com/api-keys
     *  */
    var apiKey: String by lazyLoadString()


    fun req() {
        val httpClient = Koins.get<OkHttpClient>()

        //"{\"prompt\": \"안녕하세요, GPT-4입니다. 어떻게 도와드릴까요?\", \"max_tokens\": 50}".toRequestBody(mediaType)

        val resp = httpClient.fetch {
            method = "POST"
            url = "https://api.openai.com/v1/chat/completions"
            body = obj {
                "model" to "gpt-3.5-turbo"
                "prompt" to "잘 되는지 확인해줘"
                "max_tokens" to 50
            }
            header = mapOf(
                "Authorization" to "Bearer $apiKey",
                //"OpenAI-Organization" to "org-xx",
            )
        }


        println(resp.respText)

    }


}