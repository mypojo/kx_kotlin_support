package net.kotlinx.ai

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatResponseFormat
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.LazyAwsClientProperty
import net.kotlinx.aws.bedrock.BedrockModels
import net.kotlinx.aws.bedrock.BedrockTextClient
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.aws.ssm.ssmStore
import net.kotlinx.core.Kdsl
import net.kotlinx.lazyLoad.default
import net.kotlinx.openAi.OpenAiClient
import net.kotlinx.openAi.OpenAiModels
import net.kotlinx.reflect.name

/**
 * 다양한 모델을 한번에 테스트하기위한 간이 도구
 *  */
@OptIn(BetaOpenAI::class)
class AiModelSet {

    @Kdsl
    constructor(block: AiModelSet.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 설정 ======================================================

    /** AWS 클라이언트 */
    var aws: AwsClient by LazyAwsClientProperty()

    /** 프롬프트 */
    lateinit var prompt: Any

    /** 배드락 벌크처리에 사용되는 저장소 */
    var bedrockRoot by default { S3Data("${aws.awsConfig.profileName}-work-dev", "bedrock") }

    //==================================================== 키 저장소 ======================================================

    var ssmGpt = "/secret/api/${OpenAiModels.Gpt::class.name()}"
    var ssmPerplexity = "/secret/api/${OpenAiModels.Perplexity::class.name()}"
    var ssmDeepseek = "/secret/api/${OpenAiModels.Deepseek::class.name()}"

    //==================================================== 오픈 API ======================================================

    val gpt4O by lazy {
        OpenAiClient {
            apiKey = aws.ssmStore[ssmGpt]
            model = OpenAiModels.Gpt.GPT_4O
            systemMessage = prompt.toString()
        }
    }

    val gpt4OMini by lazy {
        OpenAiClient {
            apiKey = aws.ssmStore[ssmGpt]
            model = OpenAiModels.Gpt.GPT_4O_MINI
            systemMessage = prompt.toString()
        }
    }

    val perplexity01 by lazy {
        OpenAiClient {
            apiKey = aws.ssmStore[ssmPerplexity]
            host = OpenAiModels.Perplexity.HOST
            model = OpenAiModels.Perplexity.SONAR01
            systemMessage = prompt.toString()
            responseFormat = ChatResponseFormat.Companion.Text //단순 JSON 지원 안함.  스키마가 있어가 해야하는듯?
        }
    }

    val perplexity02 by lazy {
        OpenAiClient {
            apiKey = aws.ssmStore[ssmPerplexity]
            host = OpenAiModels.Perplexity.HOST
            model = OpenAiModels.Perplexity.SONAR02
            systemMessage = prompt.toString()
            responseFormat = ChatResponseFormat.Companion.Text //단순 JSON 지원 안함.  스키마가 있어가 해야하는듯?
        }
    }
    val perplexity03 by lazy {
        OpenAiClient {
            apiKey = aws.ssmStore[ssmPerplexity]
            host = OpenAiModels.Perplexity.HOST
            model = OpenAiModels.Perplexity.SONAR03
            systemMessage = prompt.toString()
            responseFormat = ChatResponseFormat.Companion.Text //단순 JSON 지원 안함.  스키마가 있어가 해야하는듯?
        }
    }
    val perplexity04 by lazy {
        OpenAiClient {
            apiKey = aws.ssmStore[ssmPerplexity]
            host = OpenAiModels.Perplexity.HOST
            model = OpenAiModels.Perplexity.SONAR04
            systemMessage = prompt.toString()
            responseFormat = ChatResponseFormat.Companion.Text //단순 JSON 지원 안함.  스키마가 있어가 해야하는듯?
        }
    }

    val deepseek by lazy {
        OpenAiClient {
            apiKey = aws.ssmStore[ssmDeepseek]
            host = OpenAiModels.Deepseek.HOST
            model = OpenAiModels.Deepseek.CHAT
            systemMessage = prompt.toString()
            //responseFormat = ChatResponseFormat.Text  // 텍스트는 잘되는에 이미지는 422 Unprocessable Entity 오류남
        }
    }

    //==================================================== 아마존 배드락 ======================================================

    val claudeSonet by lazy {
        BedrockTextClient {
            client = aws
            model = BedrockModels.OnDemand.CLAUDE_35_SONNET
            this.systemPrompt = prompt
            batchWorkPath = bedrockRoot
            batchRole = "app-admin"
        }
    }

    val claudeHaiku by lazy {
        BedrockTextClient {
            client = aws
            model = BedrockModels.OnDemand.CLAUDE_3_HAIKU
            this.systemPrompt = prompt
            batchWorkPath = bedrockRoot
            batchRole = "app-admin"
        }
    }

    val novaPro by lazy {
        BedrockTextClient {
            client = aws
            model = BedrockModels.CrossRegion.NOVA_PRO
            systemPrompt = prompt
        }
    }

    val novaLite by lazy {
        BedrockTextClient {
            client = aws
            model = BedrockModels.CrossRegion.NOVA_LITE
            systemPrompt = prompt
        }
    }


}