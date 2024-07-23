package net.kotlinx.excel

import com.google.common.collect.Lists
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.system.ResourceHolder
import java.io.File

class ExcelTest : BeSpecLog() {

    init {
        initTest(KotestUtil.SLOW)

        Given("Excel") {
            Then("엑셀 쓰기") {
                val xls = Excel()
                xls.createSheet("G마켓상세").apply {
                    addHeader(Lists.newArrayList("상품명", "노출", "클릭"))
                    writeLine(arrayOf("치약", 10000, 50))
                    writeLine(arrayOf("가방", 25000, 80))
                    writeLine(arrayOf("당나귀", 25000, 80))
                    writeLine(arrayOf("", "", "", ""))
                    writeLine(
                        arrayOf(
                            XlsHyperlink("aa") {
                                this.urlLink = "https://www.naver.com"
                            },
                            "",
                            "",
                            "",
                        )
                    )
                }

                xls.createSheet("테이터2").apply {
                    addHeader(Lists.newArrayList("상품명", "노출", "클릭"))
                    writeLine(arrayOf("치약", 10000, 50))
                }

                //래핑해주고 파일로 쓰기
                val file = File(ResourceHolder.WORKSPACE, "excel/demo.xlsx").apply { parentFile.mkdir() }
                xls.wrap().write(file)
                log.info("다음 경로에 샘플 파일이 저장됨 -> {}", file.absolutePath)

                val excellDatas = Excel.from(file).readAll().entries
                excellDatas.size shouldBeGreaterThan 0
                log.info { "엑셀 읽기.. -> ${excellDatas.size} 건" }

                file.delete() shouldBe true
            }
        }
    }

}