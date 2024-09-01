package net.kotlinx.csv

import io.kotest.matchers.shouldBe
import net.kotlinx.file.slash
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.system.ResourceHolder

internal class CsvUtilTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("CsvUtil") {

            val workspace = ResourceHolder.WORKSPACE

            Then("기본기능테스트") {
                val rows = listOf(
                    listOf(1, 2, 3, "영감님"),
                    listOf(4, 5, 6, "이동식2"),
                )

                val file1 = workspace.slash("data1.csv")
                CsvUtil.ms949Writer().writeAll(rows, file1)

                val lines = CsvUtil.ms949Reader().readAll(file1)
                lines[0][3] shouldBe "영감님"

                file1.delete() shouldBe true
            }

        }
    }


}