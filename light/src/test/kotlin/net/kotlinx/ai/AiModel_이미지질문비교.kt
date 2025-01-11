package net.kotlinx.ai

import com.aallam.openai.api.BetaOpenAI
import net.kotlinx.aws.bedrock.LandingPageInspectionUtil
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.file.slash
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.system.ResourceHolder

@OptIn(BetaOpenAI::class)
class AiModel_이미지질문비교 : BeSpecHeavy() {

    private val root = ResourceHolder.WORKSPACE.parentFile.slash("AI").slash("bedrock_랜딩페이지모니터링")

    private val s3Root = S3Data.Companion.parse("s3://${findProfile97}-static-dev/bedrock")

    private val files = listOf(
        "cp_event.png",
        "cp_x.png",
        "cp_item01.png",
    )

    private val imageFile = files.map {
        AiTextInput.AiTextInputFile(file = root.slash(it), url = s3Root.slash(it).toPublicLink())
    }

    init {
        initTest(KotestUtil.IGNORE)

        Given("LLM 이미지 모델 비교") {

            val set = AiModelLocalSet {
                aws = aws97
                systemPrompt = LandingPageInspectionUtil.SYSTEM_PROMPT_CD
                clients = listOf(
                    gpt4O,
                    //gpt4OMini, //가끔 품절 아닌데 품절로 뜸
                    //claudeSonet,  //잘되는데 비쌈
                    //claudeHaiku, //프롬프트 인식 불가
                    //deepseek  //에러남
                )
            }

            Then("간단질의") {
                val file = imageFile[0]
                val result = set.clients[0].invokeModel(listOf(file))
                listOf(result).printSimple()
            }

            Then("배치실행") {
                val inputs = listOf(
                    listOf(imageFile[0]),
                    listOf(imageFile[1]),
                    listOf(imageFile[2]),
                )
                set.claudeSonet.invokeModelBatchAndWaitCompleted("test", inputs)
            }

            Then("모델별 비교분석") {
                val results = imageFile.flatMap { input ->
                    set.clients.map { client ->
                        suspend {
                            //퍼블렉시티의 경우 파일만 첨부가 불가능. 반드시 텍스트가 같이 입력되어야함
                            client.invokeModel(listOf(input)).also {
                                it.name = input.file?.name ?: "-"
                            }
                        }
                    }
                }.coroutineExecute()
                results.printSimple()
            }
        }
    }

}