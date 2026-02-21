package net.kotlinx.google.sheet

import io.kotest.matchers.shouldBe
import net.kotlinx.google.GoogleService
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.modules.BeSpecHeavy

class GoogleSheetTest : BeSpecHeavy() {
    init {


        given("GoogleSheet test") {

            val googleService = koin<GoogleService>()
            val testSheetId = "13U-VKClgbbwhic6Jb6nsf9ITeESn7nZiEXNN6M5fsNY" // Google's example spreadsheet ID

            then("create a new tab if it doesn't exist") {
                val tabName = "TestTab_${System.currentTimeMillis()}"
                val googleSheet = GoogleSheet(googleService, testSheetId, tabName)

                // 시트 생성 실행
                googleSheet.createTab()

                // 생성 확인
                val tabs = GoogleSheet.allTabNames(googleService.sheets, testSheetId)
                tabs.contains(tabName) shouldBe true
            }

            then("write and read values from sheet") {
                val tabName = "TestTab_${System.currentTimeMillis()}"
                val googleSheet = GoogleSheet(googleService, testSheetId, tabName)
                googleSheet.createTab()

                val testValues = listOf(
                    listOf("Header1", "Header2"),
                    listOf("Row1Col1", "Row1Col2")
                )
                googleSheet.write(testValues)

                val readValues = googleSheet.readAll()
                readValues.size shouldBe 2
                readValues[0][0] shouldBe "Header1"
            }

            then("append values to sheet") {
                val tabName = "TestTab_A"
                val googleSheet = GoogleSheet(googleService, testSheetId, tabName)
                googleSheet.createTab()

                val initialValues = listOf(listOf("Header1", "Header2"))
                googleSheet.write(initialValues)

                val appendValues = listOf(
                    listOf("Row1Col1", "Row1Col2"),
                    listOf("Row2Col1", "Row2Col2")
                )
                googleSheet.writeAppend(appendValues)

                val readValues = googleSheet.readAll()
                readValues.size shouldBe 3
                readValues[1][0] shouldBe "Row1Col1"
                readValues[2][1] shouldBe "Row2Col2"
            }
        }
    }
}
