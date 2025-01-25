package net.kotlinx.openAi

import com.aallam.openai.api.BetaOpenAI
import net.kotlinx.ai.printSimple
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.reflect.name

@OptIn(BetaOpenAI::class)
class OpenAiClient_단순채팅 : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        val query = listOf(
            "kotlin 용 DI 프레임워크 유명한고 4개만 소개해주고, 장단점을 알려줘",
            "아이들에게 교육하는 말투로 답변해줘",
            "반드시! 결과전체를 하나의 JSON으로 만들고 json의 value에는 한글 / 영어로만 답변해줘", //퍼블렉시티는 본문에 이게 들어가야함
        ).joinToString("\n")

        Given("GPT") {
            val client by koinLazy<OpenAiClient>(OpenAiModels.Gpt::class.name())
            Then("채팅") {
                val result = client.text(query)
                listOf(result).printSimple()
            }
        }

        Given("perplexity") {
            val client by koinLazy<OpenAiClient>(OpenAiModels.Perplexity::class.name())
            Then("채팅") {
                val result = client.text(query)
                listOf(result).printSimple()
            }
        }
    }

}