package net.kotlinx.openAi

import com.aallam.openai.api.BetaOpenAI
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.reflect.name

@OptIn(BetaOpenAI::class)
class OpenAiClient_파일 : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("벡터스토어") {
            val client by koinLazy<OpenAiClient>(OpenAiModels.Gpt::class.name())
            Then("리스팅") {
                val stores = client.ai.vectorStores()
                stores.printSimple()
                //stores.forEach { println(it) }
            }
            Then("정리") {
                val stores = client.ai.vectorStores()
                stores.printSimple()
                val deletes = stores.filter { it.name?.contains("데모") ?: false }
                log.info { "전체 스토어 ${stores.size} 중 사용자데이터 ${deletes.size}건 삭제됨" }
                deletes.forEach { client.ai.delete(it.id) }
            }

            Given("파일") {
                val client by koinLazy<OpenAiClient>(OpenAiModels.Gpt::class.name())
                Then("리스팅") {
                    val files = client.ai.files()
                    files.printSimple()
                }

                Then("파일 정리") {
                    val files = client.ai.files()
                    val targets = files.filter { it.purpose.raw == "user_data" }
                    targets.forEach { client.ai.delete(it.id) }
                    log.info { "전체 파일 ${files.size} 중 사용자데이터 ${targets.size}건 삭제됨" }
                }
            }

        }


    }
}