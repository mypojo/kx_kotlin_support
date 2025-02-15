package net.kotlinx.ai.demo.tnd

import com.aallam.openai.api.BetaOpenAI
import com.lectra.koson.arr
import net.kotlinx.ai.AiModelLocalSet
import net.kotlinx.ai.printSimple
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.csv.writeCsvLines
import net.kotlinx.excel.readExcellLines
import net.kotlinx.file.slash
import net.kotlinx.file.slashDir
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.retry.RetryTemplate
import net.kotlinx.string.CharSets
import net.kotlinx.system.ResourceHolder
import net.kotlinx.time.TimeFormat

@OptIn(BetaOpenAI::class)
class 광고문구01_퍼플렉시티01 : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("GPT") {

            val set = AiModelLocalSet {
                aws = aws97
                this.systemPrompt = DemoTndUtil.SYSTEM_PROMPT
                /** 벤더별 기능작동 테스트 */
                clients = listOf(
//                    gpt4OMini,
//                    claudeHaiku,
                    perplexity01,
                    perplexity02,
//                    perplexity03,
//                    perplexity04,
                )
            }

            Then("단건") {
                val query = arr[
                    "https://only.webhard.co.kr/",
                    "LG유플러스 공식사이트를 강조",
                    "저렴한 요금제와 제품의 특장점을 강조",
                ]
                val results = set.executeSingle(query.toString())
                results.toDemoTndLine().printSimple()
            }

            Then("대량") {
                val retry = RetryTemplate {}
                val workRoot = ResourceHolder.WORKSPACE.slash("AI/TND추출")
                val fileName = "검색광고_AI솔루션_T&D자동화_템플릿(커머스)_250211.xlsx"
                //val file = ResourceHolder.WORKSPACE.slash("AI/TND추출/검색광고_AI솔루션_T&D자동화_템플릿 예시.xlsx")
                val file = workRoot.slash(fileName)

                //val lines = file.readCsvLines(CharSets.MS949)
                val lines = file.readExcellLines().values.first()

                val header = lines.take(1).first()
                val model = set.perplexity03
                //val model = set.perplexity02
                val results = lines.drop(1).take(10000) .map { line ->
                    suspend {
                        val query = arr[
                            line[0],
                            line[1],
                            line[2],
                        ]
                        retry.withRetry { model.text(query.toString()).checkOrThrow() }

                    }
                }.coroutineExecute()
                results.printSimple()

                val workDir = file.parentFile.slashDir(file.nameWithoutExtension)

                val resultHeader = header + listOf("번호", "title", "desc", "검수")
                val allLines = listOf(resultHeader) + results.toDemoTndLine().map { it.liens }
                workDir.slash("${file.nameWithoutExtension}_(${model.model.name}#${TimeFormat.YMDHMS_F02.get()}).csv").writeCsvLines(allLines, CharSets.MS949)


            }
        }

    }


}