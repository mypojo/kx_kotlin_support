package net.kotlinx.openAi

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import io.ktor.util.*
import mu.KotlinLogging
import net.kotlinx.ai.AiModel
import net.kotlinx.ai.AiTextClient
import net.kotlinx.ai.AiTextInput.AiTextInputFile
import net.kotlinx.ai.AiTextResult
import net.kotlinx.core.Kdsl
import net.kotlinx.json.gson.ResultGsonData
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.lazyLoad.lazyLoadString
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * https://github.com/aallam/openai-kotlin
 * 멀티플랫폼용 client의 래퍼
 *
 * 가능하면 AWS bedrock 같은걸 쓰는게 정신건강에 좋음
 *  */
@BetaOpenAI
class OpenAiClient : AiTextClient {

    @Kdsl
    constructor(block: OpenAiClient.() -> Unit = {}) {
        apply(block)
    }

    private val log = KotlinLogging.logger {}

    /**
     * 서비스 벤더
     * 이거 단위로 API key가 달라짐으로, 여러개 사용시 복수 등록해야함
     *  */
    var host = OpenAIHost.OpenAI

    /**
     * API key
     * https://platform.openai.com/api-keys
     * https://www.perplexity.ai/settings/api
     *  */
    var apiKey: String by lazyLoadString()

    /**
     * 요청시 응답 타임아웃
     * 모델에 따라 매우 다르게 리턴됨
     *  */
    var timeout: Duration = 120.seconds

    /**
     * 접속시 타임아웃
     * GPT의 경우 60초 해도 타임아웃 날때 있음
     *  */
    var connectTimeout = 120.seconds

    /** 실제 클라이언트 */
    val ai: OpenAI by lazy {
        val config = OpenAIConfig(
            token = apiKey,
            host = host,
            timeout = Timeout(
                connect = connectTimeout,
                socket = connectTimeout,
                request = timeout
            ),
            logging = LoggingConfig(
                logger = Logger.Empty //기본 로거 사용하면, println 으로 로그가 다수 찍힌다. 이것을 방지
            ),
//            httpClientConfig = {
//                expectSuccess = false
//                this.useDefaultTransformers = false
//            },
        )
        OpenAI(config)
    }

    //==================================================== 편의기능 ======================================================

    /** 디폴트 모델 ID */
    override var model: AiModel = OpenAiModels.Gpt.GPT_4O_MINI

    /**
     * 디폴트 응답 포맷
     * 아직 구조화된건 지원안함 -> system 메세지로 json 구조를 명시해줘야함
     * 참고링크
     * https://platform.openai.com/docs/guides/structured-outputs
     *  */
    var responseFormat: ChatResponseFormat = ChatResponseFormat.JsonObject

    /**
     * 0~2 사이의 값 입력
     * GPT의 경우 0.7이 기본
     * */
    var temperature: Double? = null

    //==================================================== 디폴트 시스템 메세지 ======================================================

    /**
     * 어시스턴트 ID
     * 메세지 쓰는경우 필수.  (시스템 메세지, 기본 첨부파일 등을 포함함)
     *  */
    var assistantId: String? = null

    /**
     * 시스템 메세지
     * 채팅에 사용됨
     *  */
    var systemMessage: Any? = null

    override suspend fun invokeModel(input: List<Any>): AiTextResult {
        check(input.isNotEmpty())

        val start = System.currentTimeMillis()

        val systemPrompt = systemMessage?.let { ChatMessage(role = ChatRole.System, content = it.toString()) }

        val userMessages = convertToUserMessage(input)

        val allMessages = listOfNotNull(systemPrompt, userMessages)
        if (log.isTraceEnabled) {
            allMessages.forEach {
                log.trace { " -> ${it}" }
            }
        }

        val reqs = ChatCompletionRequest(
            model = ModelId(model.id),
            messages = allMessages,
            responseFormat = responseFormat,
            temperature = temperature
            //maxTokens = 2000,  //PX는 이거 넣으면 고장남..
        )

        val completion = try {
            ai.chatCompletion(reqs)
        } catch (e: Exception) {
            return AiTextResult.fail(model, input, e)
        }

        val usage = completion.usage!!
        log.trace {
            val usageText = "${usage.promptTokens} + ${usage.completionTokens} = ${usage.totalTokens}"
            " -> [${model.id}] 결과 ${completion.choices.size}건 -> $usageText"
        }

        check(completion.choices.size == 1) { "system용 결과는 1개로만 가정" }

        val fitstContent = completion.choices.first().message.messageContent!!
        val gson = when (fitstContent) {
            is TextContent -> {
                val text = fitstContent.content
                val replaced = when {
                    text.contains("```json") -> text.substringBetween("```json" to "```")
                    else -> text
                }
                replaced.toGsonData()
            }

            else -> {
                log.warn { "!!! 문자열 형식 확인필요 !!! -> ${fitstContent::class}" }
                fitstContent.toString().toGsonData()
            }
        }

        val duration = System.currentTimeMillis() - start
        val result = if (gson.isObject) ResultGsonData(true, gson) else ResultGsonData(false, gson)
        return AiTextResult(model, input, result, usage.promptTokens!!, usage.completionTokens!!, duration)
    }

    private fun convertToUserMessage(messages: List<Any>): ChatMessage {

        if (messages.size == 1) {
            val msg = messages.first()
            if (msg is CharSequence) {
                return ChatMessage(role = ChatRole.User, content = msg.toString())
            }
        }

        return ChatMessage(
            role = ChatRole.User, messageContent = ListContent(
                messages.map { msg ->
                    when (msg) {
                        is AiTextInputFile -> {
                            val url = msg.url ?: throw IllegalStateException("OpenAi Input url Not Found")
                            ImagePart(url)
                        }

                        else -> TextPart(msg.toString())
                    }
                }
            )
        )
    }

    /** 스래드로 작업 */
    fun thread(assistantId: String = this.assistantId!!, threadId: String = UUID.randomUUID().toString()): OpenAiThread = OpenAiThread(this, assistantId, threadId)


}