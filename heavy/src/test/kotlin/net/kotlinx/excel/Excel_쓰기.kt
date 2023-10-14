package net.kotlinx.excel

import com.google.common.collect.Lists
import net.kotlinx.core.test.TestRoot
import net.kotlinx.core.threadlocal.ResourceHolder
import org.junit.jupiter.api.Test
import java.io.File

class Excel_쓰기 : TestRoot() {


    @Test
    fun test() {

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
            //getHyperlink().addDocLink(0, "통합리포트", 0, 1)
        }

        xls.createSheet("테이터2").apply {
            addHeader(Lists.newArrayList("상품명", "노출", "클릭"))
            writeLine(arrayOf("치약", 10000, 50))
        }

        //래핑해주고 파일로 쓰기

        //래핑해주고 파일로 쓰기
        val file = File(ResourceHolder.getWorkspace(), "excel/demo.xlsx").apply { parentFile.mkdir() }
        xls.wrap().write(file)
        log.info("다음 경로에 샘플 파일이 저장됨 -> {}", file.absolutePath)
    }

}