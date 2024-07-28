package net.kotlinx.csv

import net.kotlinx.file.slash
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.system.ResourceHolder

class CsvLineDistinctTest : BeSpecLog() {

    init {
        initTest(KotestUtil.FAST)

        Given("CSV 간단 파싱") {

            val workspace = ResourceHolder.WORKSPACE
            val datas = workspace.slash("datas.csv")

            Then("파일 준비") {
                val rows = listOf(
                    listOf("h1", "h2", "h3", "이름"),
                    listOf(1, 2, 3, "영감님"),
                    listOf(4, 5, 6, "이동식2"),
                    listOf(4, 5, 12, "이동식2"),
                )
                datas.writeCsvLines(rows)
            }

            Then("파일 읽기") {

                val aggregation = CsvLineAggregation {
                    sumIndexs = setOf(0, 2)
                    distinctIndexs = setOf(3)
                    skipCnt = 1
                }
                datas.readCsvLines(callback = aggregation)

                log.info { "${aggregation.results[0].sum}" }
                log.info { "${aggregation.results[2].sum}" }
                log.info { "${aggregation.results[3].distinct}" }


            }
        }
    }

}
