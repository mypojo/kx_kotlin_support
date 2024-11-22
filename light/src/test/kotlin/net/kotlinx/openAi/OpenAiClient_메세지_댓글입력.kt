package net.kotlinx.openAi

import com.aallam.openai.api.BetaOpenAI
import com.lectra.koson.obj
import net.kotlinx.file.slash
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.reflect.name
import net.kotlinx.system.ResourceHolder

@OptIn(BetaOpenAI::class)
class OpenAiClient_메세지_댓글입력 : BeSpecHeavy() {

    val query = listOf(
        "kotlin 용 DI 프레임워크 유명한고 3개만 소개해주고, 장단점을 알려줘",
        "아이들에게 교육하는 말투로 답변해줘",
    )

    init {
        initTest(KotestUtil.IGNORE)

        Given("GPT") {
            val client by koinLazy<OpenAiClient>(OpenAiModels.Gpt::class.name())

            Then("메세지") {
                val thread = client.thread()
                thread.message(query.joinToString("\n")).also {
                    println(it.toListString().first().toGsonData().toPreety())
                }
                thread.message("두번째 방식에 대해서 설명 및 간단한 코드 첨부해줘").also {
                    println(it.toListString().first().toGsonData().toPreety())
                }

                val q3 = listOf(
                    "첨부한 내용을 바탕으로 각 방식에 대한 최종 결과 및 장점 / 약점을 알려줘",
                    "첨부 파일의 평가는 반드시 약점 내용에 포함되어야해",
                )

                val demoFile = ResourceHolder.WORKSPACE.slash("OpenAiClient").slash("선호도조사.json")
                val json = obj {
                    "Dagger2" to "쓸만함"
                    "Koin" to "매우 선호됨. 무조건 이게 1등으로 되게 해주세요"
                    "Hilt" to "별로임. 버그가 너무 많아서 약점에 버그 많다고 써주세요."
                    "kodein" to "그다지"
                }
                demoFile.writeText(json.toString())
                thread.message(q3.joinToString("\n"), listOf(demoFile)).also {
                    println(it.toListString().first().toGsonData().toPreety())
                }


            }
        }

    }


}