package net.kotlinx.openAi

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import mu.KotlinLogging
import net.kotlinx.core.Kdsl
import net.kotlinx.core.time.TimeStart
import net.kotlinx.props.lazyLoadString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * https://github.com/aallam/openai-kotlin
 * 멀티플랫폼용 client의 래퍼
 *
 * GPT는 요금제가 2가지?
 *  웹 콘솔 = 월 과금
 *  API 호출 = 종량제 => 이거 사용해야함
 *  */
//@OptIn(BetaOpenAI::class) //스래드 사용
class OpenAiClient {

    @Kdsl
    constructor(block: OpenAiClient.() -> Unit = {}) {
        apply(block)
    }

    private val log = KotlinLogging.logger {}

    /**
     * API key
     * 키 받는곳 https://platform.openai.com/api-keys
     *  */
    var apiKey: String by lazyLoadString()

    /** 요청시 응답 타임아웃 */
    var timeout: Duration = 60.seconds

    /**
     * 접속시 타임아웃
     * GPT4의 경우 30초 해도 타임아웃 날때 있음
     *  */
    var connectTimeout = 60.seconds

    /** 실제 클라이언트 */
    val openAi: OpenAI by lazy {
        val config = OpenAIConfig(
            token = apiKey,
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

    /** 모델 ID */
    var modelId: String = OpenAiModels.GPT_3D5_TURBO

    /**
     * 디폴트 메세지
     * ex) 시스템 메세지(설정) 등
     *  */
    var fixedMessages: List<ChatMessage> = emptyList()

    /** 단순 채팅 질문 */
    suspend fun chat(msgs: List<String>): ChatCompletion {
        val start = TimeStart()
        val reqs = ChatCompletionRequest(
            model = ModelId(modelId),
            messages = fixedMessages + msgs.map { ChatMessage(role = ChatRole.User, content = it) },

            )
        val completion = openAi.chatCompletion(reqs)
        log.debug { " -> [$modelId] 걸린시간 $start" }
        return completion
    }

//    /**
//     * 새 스래드 생성
//     *  */
//    suspend fun newThread(): OpenAiThread = OpenAiThread(this, openAi.thread())


}