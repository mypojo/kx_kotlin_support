package net.kotlinx.spring.opencsv

import io.kotest.matchers.shouldBe
import net.kotlinx.file.*
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.reflect.name
import net.kotlinx.spring.batch.BatchExecutor
import net.kotlinx.spring.batch.component.CsvItemWriterTemplate
import net.kotlinx.spring.batch.component.toItemReader
import net.kotlinx.system.ResourceHolder
import java.io.File

class CsvItemWriterTemplateTest : BeSpecLog() {

    init {
        initTest(KotestUtil.SLOW)

        Given("CsvItemWriterTemplate") {

            val workspace = ResourceHolder.WORKSPACE.slash(CsvItemWriterTemplate::class.name())
            workspace.deleteRecursively() //주의!!
            log.info { "workspace : $workspace" }

            fun doTest(cnt: Int, file: File) {
                val context = BatchExecutor {
                    this.name = name
                    itemReader = (0 until cnt).toItemReader()
                    itemProcessor = { arrayOf("N${it}", "X${it}") }
                    itemWriter = CsvItemWriterTemplate {
                        header = arrayOf("일번", "이번")
                        this.file = file
                        limit = 100000
                    }.build()
                    commitInterval = 3
                }
                if (file.isDirectory) {
                    val result = FileZipTemplate(1).zip(file)
                    log.info { "결과 컨텍스트 ${context.executionContext} / 결과디렉토리 압축파일 : $result" }
                } else {
                    log.info { "결과 컨텍스트 ${context.executionContext} / 결과파일 : $file" }
                }

            }

            val smallCnt = 10
            val largeCnt = 300000
            When("시스템 읽기용(UTF-8) = 엑셀로 읽으면 깨짐 & gzip압축 -> 파라메터로 file 입력") {
                Then("${smallCnt}건 -> gz 파일1개 생성") {
                    val file = workspace.slash("UTF-8-${smallCnt}건.csv.gz")
                    doTest(smallCnt, file)
                    val unziped = FileGzipUtil.unGzip(file)
                    unziped.readLines().size shouldBe smallCnt + 1  //헤더 포함
                }
                Then("${largeCnt}건 -> 파일 크기에 관계없이 gz파일 1개 생성") {
                    doTest(largeCnt, workspace.slash("UTF-8-${largeCnt}건.csv.gz"))
                }
            }

            When("PC에서 읽기용(MS949) = 엑셀로 읽기 가능 -> 파라메터로 dir 입력") {
                Then("${smallCnt}건 -> 비압축 파일1개 생성") {
                    doTest(smallCnt, workspace.slashDir("MS949-${smallCnt}건"))
                }
                Then("${largeCnt}건 -> 디렉토리 압축 파일1개 생성 (내부에 x개의 파일 있음)") {
                    doTest(largeCnt, workspace.slashDir("MS949-${largeCnt}건"))
                }
            }

            Then("임시테스트") {
                val dir = ResourceHolder.WORKSPACE.slash("aceCounterDirectDbJob_69660001")
                val zip = dir.slash("dbdirect_2024_12_03-14_23.csv").gzip()
                println(zip.absolutePath)
            }
        }
    }

}
