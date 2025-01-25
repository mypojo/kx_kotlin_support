package net.kotlinx.ai

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatResponseFormat
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.bedrock.BedrockModels
import net.kotlinx.aws.bedrock.BedrockTextClient
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.aws.ssm.ssmStore
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.core.Kdsl
import net.kotlinx.openAi.OpenAiClient
import net.kotlinx.openAi.OpenAiModels
import net.kotlinx.reflect.name

/** 각종 모델 테스트용 */
@OptIn(BetaOpenAI::class)
class AiModelLocalSet {

    @Kdsl
    constructor(block: AiModelLocalSet.() -> Unit = {}) {
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
                it.text(query)
            }
        }.coroutineExecute(coroutineLimit)
    }

    val bedrockRoot by lazy { S3Data("${aws.awsConfig.profileName}-work-dev", "bedrock") }

    //==================================================== 오픈 API ======================================================

    val gpt4O by lazy {
        OpenAiClient {
            apiKey = aws.ssmStore[KEY_GPT]
            model = OpenAiModels.Gpt.GPT_4O
            systemMessage = this@AiModelLocalSet.systemPrompt!!.toString()
        }
    }

    val gpt4OMini by lazy {
        OpenAiClient {
            apiKey = aws.ssmStore[KEY_GPT]
            model = OpenAiModels.Gpt.GPT_4O_MINI
            systemMessage = this@AiModelLocalSet.systemPrompt!!.toString()
        }
    }

    val perplexitySmall by lazy {
        OpenAiClient {
            apiKey = aws.ssmStore[KEY_PERPLEXITY]
            host = OpenAiModels.Perplexity.HOST
            model = OpenAiModels.Perplexity.SONAR_SMALL
            systemMessage = this@AiModelLocalSet.systemPrompt!!  //아직 어시스턴스 없이 채팅만 지원함. 이거 해도 안되는거 많음..
            responseFormat = ChatResponseFormat.Text //단순 JSON 지원 안함.  스키마가 있어가 해야하는듯?
        }
    }

    val perplexityLarge by lazy {
        OpenAiClient {
            apiKey = aws.ssmStore[KEY_PERPLEXITY]
            host = OpenAiModels.Perplexity.HOST
            model = OpenAiModels.Perplexity.SONAR_LARGE
            systemMessage = this@AiModelLocalSet.systemPrompt!!  //아직 어시스턴스 없이 채팅만 지원함. 이거 해도 안되는거 많음..
            responseFormat = ChatResponseFormat.Text //단순 JSON 지원 안함.  스키마가 있어가 해야하는듯?
        }
    }

    val deepseek by lazy {
        OpenAiClient {
            apiKey = aws.ssmStore[KEY_DEEPSEEK]
            host = OpenAiModels.Deepseek.HOST
            model = OpenAiModels.Deepseek.CHAT
            //responseFormat = ChatResponseFormat.Text  // 텍스트는 잘되는에 이미지는 422 Unprocessable Entity 오류남
        }
    }

    //==================================================== 아마존 배드락 ======================================================

    val claudeSonet by lazy {
        BedrockTextClient {
            client = aws
            model = BedrockModels.OnDemand.CLAUDE_35_SONNET
            this.systemPrompt = this@AiModelLocalSet.systemPrompt!!
            batchWorkPath = bedrockRoot
            batchRole = "app-admin"
        }
    }

    val claudeHaiku by lazy {
        BedrockTextClient {
            client = aws
            model = BedrockModels.OnDemand.CLAUDE_3_HAIKU
            this.systemPrompt = this@AiModelLocalSet.systemPrompt!!
            batchWorkPath = bedrockRoot
            batchRole = "app-admin"
        }
    }

    companion object {

        private val KEY_GPT = "/api/${OpenAiModels.Gpt::class.name()}/service/key"
        private val KEY_PERPLEXITY = "/api/${OpenAiModels.Perplexity::class.name()}/service/key"
        private val KEY_DEEPSEEK = "/api/${OpenAiModels.Deepseek::class.name()}/service/key"

    }


}