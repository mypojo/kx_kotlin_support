package net.kotlinx.gpt

import net.kotlinx.test.TestLight
import org.junit.jupiter.api.Test

class GptClientTest : TestLight() {


    @Test
    fun test() {

        val gptClient = GptClient {
            //apiKey = LazyLoadProperty.ssm("/gpt4/demo/key")
            apiKey = "xxx"
        }

        gptClient.req()

    }

}