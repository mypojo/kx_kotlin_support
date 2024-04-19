package net.kotlinx.openAi

import kotlinx.coroutines.runBlocking
import net.kotlinx.props.LazyLoadProperty
import net.kotlinx.test.TestLight
import org.junit.jupiter.api.Test

class OpenAiClientTest : TestLight() {

    val openAiClient = OpenAiClient {
        apiKey = LazyLoadProperty.ssm("/gpt4/demo/key")
        modelId = OpenAiModels.GPT_4
    }

    @Test
    fun test() = runBlocking {

        val completion = openAiClient.chat(
            listOf(
                "kotlin 용 DI 프레임워크 유명한고 4개만 소개해주고, 장단점을 알려줘",
                "아이들에게 교육하는 말투로 답변해줘",
            )
        )

        val contents = completion.toContents()
        contents.forEachIndexed { index, s ->
            log.info { "결과 ${index + 1}/${contents.size} \n$s" }
        }

    }
}