package net.kotlinx.ai

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatResponseFormat
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.bedrock.BedrockModels
import net.kotlinx.aws.bedrock.BedrockRuntime
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.core.Kdsl
import net.kotlinx.core.ProtocolPrefix
import net.kotlinx.openAi.OpenAiClient
import net.kotlinx.openAi.OpenAiModels
import net.kotlinx.reflect.name

/** 각종 모델 테스트용 */
@OptIn(BetaOpenAI::class)
class AiModelSet {

    @Kdsl
    constructor(block: AiModelSet.() -> Unit = {}) {
        apply(block)
    }
    //==================================================== 설정 ======================================================

    var systemPrompt: Any? = null

    lateinit var aws: AwsClient

    lateinit var clients: List<AiTextClient>

    var coroutineLimit = 20

    //==================================================== 실행 ======================================================

    /**
     * 간단 실행
     * 실제 실행은 더 복잡한 입력/수정의 파싱 작업이 필요하다.
     *  */
    fun executeSingle(query: String): List<AiTextResult> {
        return clients.map {
            suspend {
                it.chat(query)
            }
        }.coroutineExecute(coroutineLimit)
    }


    //==================================================== 내장 모델들 ======================================================

    val gpt4O by lazy {
        OpenAiClient {
            apiKey = KEY_GPT
            model = OpenAiModels.Gpt.GPT_4O
            systemMessage = systemPrompt!!.toString()
        }
    }

    val gpt4OMini by lazy {
        OpenAiClient {
            apiKey = KEY_GPT
            model = OpenAiModels.Gpt.GPT_4O_MINI
            systemMessage = systemPrompt!!.toString()
        }
    }

    val perplexitySmall by lazy {
        OpenAiClient {
            apiKey = KEY_PERPLEXITY
            host = OpenAiModels.Perplexity.HOST
            model = OpenAiModels.Perplexity.SONAR_SMALL
            systemMessage = systemPrompt!!  //아직 어시스턴스 없이 채팅만 지원함. 이거 해도 안되는거 많음..
            responseFormat = ChatResponseFormat.Text //단순 JSON 지원 안함.  스키마가 있어가 해야하는듯?
        }
    }

    val perplexityLarge by lazy {
        OpenAiClient {
            apiKey = KEY_PERPLEXITY
            host = OpenAiModels.Perplexity.HOST
            model = OpenAiModels.Perplexity.SONAR_LARGE
            systemMessage = systemPrompt!!  //아직 어시스턴스 없이 채팅만 지원함. 이거 해도 안되는거 많음..
            responseFormat = ChatResponseFormat.Text //단순 JSON 지원 안함.  스키마가 있어가 해야하는듯?
        }
    }

    val claudeSonet by lazy {
        BedrockRuntime {
            client = aws
            model = BedrockModels.OnDemand.CLAUDE_35_SONNET
            system = systemPrompt!!
        }
    }

    val claudeHaiku by lazy {
        BedrockRuntime {
            client = aws
            model = BedrockModels.OnDemand.CLAUDE_3_HAIKU
            system = systemPrompt!!
        }
    }

    companion object {
        private val KEY_GPT = "${ProtocolPrefix.SSM}/api/${OpenAiModels.Gpt::class.name()}/demo/key"
        private val KEY_PERPLEXITY = "${ProtocolPrefix.SSM}/api/${OpenAiModels.Perplexity::class.name()}/demo/key"


    }


}