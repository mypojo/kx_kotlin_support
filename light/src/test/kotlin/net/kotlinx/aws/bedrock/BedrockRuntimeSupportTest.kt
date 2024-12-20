package net.kotlinx.aws.bedrock

import net.kotlinx.ai.AiTextResult
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.file.slash
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.system.ResourceHolder
import net.kotlinx.time.TimeStart
import java.io.File

class BedrockRuntimeSupportTest : BeSpecHeavy() {

    private val root = ResourceHolder.WORKSPACE.parentFile.slash("AI").slash("bedrock_랜딩페이지모니터링")
    private val imageFile = listOf(
        root.slash("cp_event.png"),
        root.slash("cp_x.png"),
        root.slash("cp_item01.png"),
    )

    init {
        initTest(KotestUtil.IGNORE)

        Given("베드락 런타임") {

            val bedrockRoot = S3Data("adpriv-work-dev", "bedrock")

            val sonet = BedrockRuntime {
                client = aws97
                model = BedrockModels.OnDemand.CLAUDE_35_SONNET
                system = LandingPageInspectionUtil.PROMPT
                workPath = bedrockRoot
                batchRole = "app-admin"
            }

            val haiku = BedrockRuntime {
                client = aws97
                model = BedrockModels.OnDemand.CLAUDE_3_HAIKU
                system = LandingPageInspectionUtil.PROMPT
                workPath = bedrockRoot
                batchRole = "app-admin"
            }

            Then("간단질의") {
                val file = imageFile[0]
                val result = sonet.invokeModel(listOf(file))
                log.info { "[${result.inputTokens}/${result.outputTokens}] -> ${result.body}" }
            }

            Then("배치실행") {
                val inputs = listOf(
                    listOf(imageFile[0]),
                    listOf(imageFile[1]),
                    listOf(imageFile[2]),
                )
                sonet.invokeModelBatchAndWaitCompleted("test", inputs)
            }

            Then("모델별 비교분석") {

                data class Result(
                    val file: File,
                    val name: String,
                    val result: AiTextResult,
                    val duration: String,
                )

                val modelMap = mapOf(
                    "sonet" to sonet,
                    "haiku" to haiku,
                )

                val results = imageFile.flatMap { file ->
                    modelMap.entries.map { e ->
                        suspend {
                            val start = TimeStart()
                            Result(
                                file,
                                e.key,
                                e.value.invokeModel(listOf(file)),
                                start.toString(),
                            )
                        }
                    }
                }.coroutineExecute()

                listOf("파일", "모델", "입력토큰", "출력토큰", "걸린시간", "결과").toTextGridPrint {
                    results.map {
                        arrayOf(it.file.name, it.name, it.result.inputTokens, it.result.outputTokens, it.duration, it.result.body)
                    }
                }
            }

        }
    }

}
