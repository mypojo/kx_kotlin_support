package net.kotlinx.koog


import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.lazyLoad.lazyLoadStringSsm


class KoogTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("KoogTest") {

            val gptKey by lazyLoadStringSsm("/secret/api/gpt")
            val perplexityKey by lazyLoadStringSsm("/secret/api/perplexity")
            val deepseek by lazyLoadStringSsm("/secret/api/deepseek")

            val query = "kotlin에서 LLM을 사용할건데, Koog를 쓸거야. 이거와 유사한 제품을 비교해줘"

            val agent = AIAgent(
                executor = simpleOpenAIExecutor(gptKey), // or Anthropic, Google, OpenRouter, etc.
                systemPrompt = "너는 kotlin 전문가야",
                llmModel = OpenAIModels.Chat.GPT4o,
            )

            Then("테스트") {
                val result = agent.run(query)
                println(result)
            }


        }
    }

}