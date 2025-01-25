package net.kotlinx.ai

import com.aallam.openai.api.BetaOpenAI
import net.kotlinx.aws.bedrock.LandingPageInspectionUtil
import net.kotlinx.aws.s3.S3Data
import net.kotlinx.aws.s3.listObjects
import net.kotlinx.aws.s3.s3
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.excel.Excel
import net.kotlinx.excel.XlsHyperlink
import net.kotlinx.file.slash
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.system.ResourceHolder
import net.kotlinx.time.TimeFormat

@OptIn(BetaOpenAI::class)
class AiModel_이미지_랜딩확인_CSV저장 : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("LLM 이미지 모델 비교") {

            val set = AiModelLocalSet {
                aws = aws97
                this.systemPrompt = LandingPageInspectionUtil.SYSTEM_PROMPT_CD
                clients = listOf(gpt4O)
            }

            Then("벤더 모델비교 분석") {

                val nakId = "303270709"
                val path = S3Data("nak-real-work", "upload/task/20250103/${nakId}/")

                val orgImgs = aws97.s3.listObjects(path)
                    .filter { it.fileName.endsWith(".png") }.map {
                        AiTextInput.AiTextImage {
                            url = it.toPublicLink()
                        }
                    }

                val results = orgImgs.flatMap { input ->
                    set.clients.map { client ->
                        suspend {
                            //퍼블렉시티의 경우 파일만 첨부가 불가능. 반드시 텍스트가 같이 입력되어야함
                            client.text(listOf(input)).also {
                                it.name = input.url ?: "-"
                            }
                        }
                    }
                }.coroutineExecute()
                results.printSimple()

                val xls = Excel()
                xls.createSheet("랜딩결과").apply {
                    addHeader(listOf("파일명", "성공", "사유"))

                    results.map {
                        val file = (it.input as List<AiTextInput.AiTextImage>).first()
                        writeLine(
                            listOf(
                                XlsHyperlink(file.name) {
                                    this.urlLink = file.url
                                },
                                if (it.output.data["ok"].bool == true) "정상" else "비정상",
                                it.output.data["cause"].str
                            )
                        )
                    }
                }

                //래핑해주고 파일로 쓰기
                val file = ResourceHolder.WORKSPACE.slash("AI이미지").slash("랜딩모니터링_${TimeFormat.YMDHMS_F02.get()}.xlsx")
                xls.wrap().write(file)
                log.info("다음 경로에 샘플 파일이 저장됨 -> {}", file.absolutePath)


            }

        }
    }

}