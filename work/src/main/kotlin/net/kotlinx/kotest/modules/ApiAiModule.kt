package net.kotlinx.kotest.modules

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatResponseFormat
import mu.KotlinLogging
import net.kotlinx.core.ProtocolPrefix
import net.kotlinx.koin.KoinModule
import net.kotlinx.openAi.OpenAiClient
import net.kotlinx.openAi.OpenAiModels
import net.kotlinx.reflect.name
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** 해당 패키지의 기본적인 의존성 주입 */
object ApiAiModule : KoinModule {

    private val log = KotlinLogging.logger {}

    @OptIn(BetaOpenAI::class)
    override fun moduleConfig(): Module = module {
        val systemMsgs = listOf(
            "코드 작성언어 = kotlin",
            "AWS 사용 SDK = kotlin SDK",
            "반드시! 결과전체를 하나의 JSON으로 만들고 json의 value에는 한글 / 영어로만 답변해줘",
            "결과에 마크다운 , 이모지, 한자 사용 금지", //퍼블렉시티..
        )
        single(named(OpenAiModels.Gpt::class.name())) {
            OpenAiClient {
                apiKey = "${ProtocolPrefix.SSM}/api/${OpenAiModels.Gpt::class.name()}/demo/key"
                model = OpenAiModels.Gpt.GPT_4O_MINI
                systemMessage = systemMsgs.joinToString("\n")
            }
        }
        single(named(OpenAiModels.Perplexity::class.name())) {
            OpenAiClient {
                apiKey = "${ProtocolPrefix.SSM}/api/${OpenAiModels.Perplexity::class.name()}/demo/key"
                host = OpenAiModels.Perplexity.HOST
                model = OpenAiModels.Perplexity.SONAR_SMALL
                //modelId = OpenAiModels.Perplexity.SONAR_HUGE
                systemMessage = systemMsgs.joinToString("\n")  //아직 어시스턴스 없이 채팅만 지원함. 이거 해도 안되는거 많음..
                responseFormat = ChatResponseFormat.Text //단순 JSON 지원 안함.  스키마가 있어가 해야하는듯?
            }
        }

    }

}