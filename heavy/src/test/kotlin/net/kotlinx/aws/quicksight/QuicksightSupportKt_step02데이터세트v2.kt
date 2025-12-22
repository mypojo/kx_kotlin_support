package net.kotlinx.aws.quicksight

import net.kotlinx.aws.athena.rowLines
import net.kotlinx.aws.quicksight.QuicksightDataSetCreation.QuicksightDataSetConfigType
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print
import net.kotlinx.string.replaceAll
import java.util.*


class QuicksightSupportKt_step02데이터세트v2 : BeSpecHeavy() {


    val ADMINS = listOf("DEV/sin", "dladmedia12@gmail.com")

    init {
        initTest(KotestUtil.IGNORE)

        Given("최초 생성된 데모 삭제하기") {

            Then("데이터세트 리스팅") {
                val items = aws49.quicksight.listDataSets()
                items.printSimple()
            }

            Then("데이터세트 생성 by sql") {

                val sql = """
                    SELECT 
                        basic_date AS "날짜",
                        media_div AS "매체구분",
                        SUM(COALESCE(rpt.imp, 0)) AS "노출수",
                        SUM(COALESCE(rpt.click, 0)) AS "클릭수",
                        SUM(COALESCE(rpt.adspend, 0)) AS "광고비",
                        CASE 
                            WHEN SUM(COALESCE(rpt.adspend, 0)) > 0 
                            THEN round(100.0 * SUM(COALESCE(conv['conv_ca'], 0)) / SUM(COALESCE(rpt.adspend, 0)), 2) 
                            ELSE 0 
                        END AS "ROAS"
                    FROM "s3tablescatalog/dmp"."d3"."rpt_agroup"
                    WHERE basic_date BETWEEN ':startDate' AND ':endDate'
                    GROUP BY basic_date, media_div
                    ORDER BY basic_date, media_div
                """.trimIndent()

                val queryParam = mapOf(
                    ":startDate" to "20251201",
                    ":endDate" to "20251213",
                )


                val sqlQuery = sql.replaceAll(queryParam)
                val queryResults = athenaModule49.executeAndgetQueryResults("$sqlQuery", 4)
                queryResults.resultSet!!.rowLines.print()

                val sqlQueryMetadatas = queryResults.resultSet!!.resultSetMetadata!!.columnInfo!!

                val suff = "dev"
                val setConfig = QuicksightDataSetCreation {
                    this.dataSourceId = "athena-dev"
                    this.dataSetId = "nlRpt-${UUID.randomUUID()}"
                    this.dataSetName = "전체미디어종합-${suff} v2"
                    type = QuicksightDataSetConfigType.QUERY
                    users = ADMINS
                    query = sqlQuery
                    this.columns = sqlQueryMetadatas.associate { info ->
                        val name = requireNotNull(info.name) { "컬럼 이름이 없습니다" }
                        val type = requireNotNull(info.type) { "컬럼 타입이 없습니다 ($name)" }
                        name to type.toQuickSightType()
                    }
                }
                aws49.quicksight.createDataSetV2(setConfig)
            }


            Then("데이터세트 삭제") {
                val items = aws49.quicksight.listDataSets()
                items.printSimple()
                items.filter { it.name in setOf("People Overview", "Web and Social Media Analytics", "Business Review", "Sales Pipeline") }.forEach {
                    aws49.quicksight.deleteDataSet(it.dataSetId!!)
                    log.warn { "데이터세트 ${it.name} 삭제 완료" }
                }
            }
        }
    }

}
