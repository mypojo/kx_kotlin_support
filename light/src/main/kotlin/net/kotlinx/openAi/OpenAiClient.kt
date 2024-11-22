package net.kotlinx.openAi

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.assistant.AssistantId
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.kotlinx.core.Kdsl
import net.kotlinx.lazyLoad.lazyLoadString
import net.kotlinx.time.TimeStart
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * https://github.com/aallam/openai-kotlin
 * 멀티플랫폼용 client의 래퍼
 *  */
@BetaOpenAI
class OpenAiClient {

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
            )

        )
        OpenAI(config)
    }

    //==================================================== 편의기능 ======================================================

    /** 디폴트 모델 ID */
    var modelId: String = OpenAiModels.Gpt.GPT_4O_MINI

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
    var systemMessages: List<String> = emptyList()

    private val mutex = Mutex()

    /** 단순 채팅 질문 */
    suspend fun chat(msg: String): ChatCompletion {

        mutex.withLock {
            if (systemMessages.isEmpty() && assistantId != null) {
                log.debug { " -> 시스템 메세지가 없음 -> assistantId ${assistantId} 를 시스템 메시지로 가져옴" }
                systemMessages = listOf(ai.assistant(AssistantId(assistantId!!))!!.instructions!!)
            }
        }

        val start = TimeStart()
        val systems = when {
            systemMessages.isEmpty() -> emptyList()
            else -> listOf(ChatMessage(role = ChatRole.System, content = systemMessages.joinToString("\n")))
        }
        val reqs = ChatCompletionRequest(
            model = ModelId(modelId),
            messages = systems + ChatMessage(role = ChatRole.User, content = msg),
            responseFormat = responseFormat,
            temperature = temperature
            //maxTokens = 2000,  //PX는 이거 넣으면 고장남..
        )
        log.trace { " => 시스템 멘트 : ${systems.joinToString { it.content!!.replace("\n", "/") }}" }

        val completion = ai.chatCompletion(reqs)

        val usage = completion.usage?.let { "${it.promptTokens} + ${it.completionTokens} = ${it.totalTokens}" } ?: "N/A"

        log.debug { " -> [$modelId] 결과 ${completion.choices.size}건 -> 걸린시간 $start => $usage" }
        return completion
    }

    /** 스래드로 작업 */
    fun thread(assistantId: String = this.assistantId!!, threadId: String = UUID.randomUUID().toString()): OpenAiThread = OpenAiThread(this, assistantId, threadId)


}