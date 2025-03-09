package net.kotlinx.okhttp

import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import net.kotlinx.csv.CsvReadWriteTool
import net.kotlinx.csv.CsvUtil
import net.kotlinx.csv.CsvUtil.TSV_UNOFFICIAL
import net.kotlinx.csv.toFlow
import net.kotlinx.file.slash
import net.kotlinx.io.input.toInputResource
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.number.toSiText
import net.kotlinx.string.print
import net.kotlinx.system.ResourceHolder
import okhttp3.OkHttpClient


internal class OkHttpSupport비동기파일다운로드 : BeSpecHeavy() {

    private val client by koinLazy<OkHttpClient>()

    init {
        initTest(KotestUtil.IGNORE)

        val root = ResourceHolder.WORKSPACE.slash("파일다운로드")

        Given("비동기파일 다운로드") {

            Then("파일 다운로드 - 정상 작동함") {
                val file = root.slash("text01.txt")
                file.delete()
                file.exists() shouldBe false
                client.download("https://publicobject.com/helloworld.txt", file)
                file.length() shouldBeGreaterThan 1000
            }

            val file = root.slash("naver.csv")

            When("비정상포맷 TSV를 CSV로 변환하면서 다운로드") {

                file.delete()
                file.exists() shouldBe false

                client.download("https://xx.txt") {
                    CsvReadWriteTool {
                        readerInputStream = it
                        readerFactory = { TSV_UNOFFICIAL }
                        writerFile = file
                        writerFactory = { CsvUtil.ms949Writer() }
                    }
                }
                log.info { "파일 다운로드완료 : ${file.length().toSiText()} -> ${file.absolutePath}" }
                file.length() shouldBeGreaterThan 1000
            }

            Then("정상 CSV 부분 읽기") {
                val flow = file.toInputResource().toFlow(CsvUtil.ms949Reader())
                flow.take(5).toList().print()
            }


        }
    }


}