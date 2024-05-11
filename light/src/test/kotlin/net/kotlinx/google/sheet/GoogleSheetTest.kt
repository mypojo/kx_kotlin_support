package net.kotlinx.google.sheet

import net.kotlinx.google.GoogleSecret
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

internal class GoogleSheetTest : BeSpecLog() {

    init {
        initTest(KotestUtil.SLOW)

        Given("GoogleSheet") {

            val secret = GoogleSecret {}
            val sheet = GoogleSheet(secret.createService(), "13U-VKClgbbwhic6Jb6nsf9ITeESn7nZiEXNN6M5fsNY", "테스트용")

            Then("구글시트 읽기") {
                sheet.readAll().forEachIndexed { index, line ->
                    log.debug { " -> 시트 라인 $index : $line" }
                }
            }
            Then("구글시트 쓰기") {
                val datas = listOf(
                    listOf("멍멍", "야옹"),
                    listOf("으르렁", "뽀작"),
                )
                sheet.write(datas, 2 to 4)
            }
        }
    }

}