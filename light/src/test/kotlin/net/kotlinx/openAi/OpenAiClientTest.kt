package net.kotlinx.openAi

import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class OpenAiClientTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("OpenAiClient") {
            log.warn { "비용 나오고 오래걸림!!" }
            val openAiClient = koin<OpenAiClient>()

            Then("요청 & 응답 샘플") {
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
    }

}