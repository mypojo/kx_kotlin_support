package net.kotlinx.koog


import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.bedrock.BedrockLLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.bedrock.brr
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.lazyLoad.lazyLoadStringSsm


class KoogTest : BeSpecHeavy() {

    private val aws by koinLazy<AwsClient>(findProfile49)

    init {
        initTest(KotestUtil.IGNORE)

        Given("KoogTest") {

            val systemPrompt = "너는 kotlin 전문가야"
            val query = "kotlin에서 LLM을 사용할건데, Koog를 쓸거야. 이거와 유사한 제품을 비교해줘"

            Then("GPT4o") {
                val key by lazyLoadStringSsm("/secret/api/gpt")
                val agent = AIAgent(
                    executor = simpleOpenAIExecutor(key),
                    systemPrompt = systemPrompt,
                    llmModel = OpenAIModels.Chat.GPT4o,
                )
                val result = agent.run(query)
                println(result)
            }

            Then("BedrockModels") {

                val standardCapabilities: List<LLMCapability> = listOf(
                    LLMCapability.Temperature,
                    LLMCapability.Completion
                )
                val fullCapabilities: List<LLMCapability> = standardCapabilities + listOf(
                    LLMCapability.Tools,
                    LLMCapability.ToolChoice,
                    LLMCapability.Schema.JSON.Full,
                    LLMCapability.Vision.Image
                )

                val AnthropicClaude35SonnetV2: LLModel = LLModel(
                    provider = LLMProvider.Bedrock,
                    id = "anthropic.claude-3-5-sonnet-20240620-v1:0",
                    capabilities = fullCapabilities
                )
                val asd = SingleLLMPromptExecutor(BedrockLLMClient(aws.brr))

                val agent = AIAgent(
                    executor = SingleLLMPromptExecutor(BedrockLLMClient(aws.brr)),
                    systemPrompt = systemPrompt,
                    llmModel = AnthropicClaude35SonnetV2,
                    //llmModel = BedrockModels.AmazonNovaLite,
                )
                val result = agent.run(query)
                println(result)
            }


        }
    }

}