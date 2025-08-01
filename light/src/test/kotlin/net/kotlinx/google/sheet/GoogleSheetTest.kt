package net.kotlinx.google.sheet

import net.kotlinx.google.GoogleService
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

internal class GoogleSheetTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.SLOW)

        Given("GoogleSheet") {

            val googleService by koinLazy<GoogleService>()
            val sheet = GoogleSheet(googleService, "13U-eee", "테스트용")

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