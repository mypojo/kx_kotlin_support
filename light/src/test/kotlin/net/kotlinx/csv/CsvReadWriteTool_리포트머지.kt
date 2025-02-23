package net.kotlinx.csv

import com.google.common.collect.Lists
import net.kotlinx.aws.AwsClient
import net.kotlinx.excel.Excel
import net.kotlinx.file.slash
import net.kotlinx.koin.Koins
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.number.halfUp
import net.kotlinx.number.toBigDecimal2
import net.kotlinx.reflect.fromLines
import net.kotlinx.system.ResourceHolder
import okhttp3.OkHttpClient

class CsvReadWriteTool_리포트머지 : BeSpecLight() {

    private val aws by lazy { Koins.koin<AwsClient>(findProfile97) }

    private val httpClient by Koins.koinLazy<OkHttpClient>()

    init {
        initTest(KotestUtil.IGNORE)

        val workRoot = ResourceHolder.WORKSPACE.slash("리포트머지")
        val file1 = workRoot.slash("a.csv")
        val file2 = workRoot.slash("b.csv")

        val resultFile = workRoot.slash("키워드리포트.xlsx")

        Given("키워드 리포트 두개읽고 통계치 합산") {

            data class RptData(
                val kwd: String,
                val imp: Long,
                val click: Long,
                val adpsend: Long,
                val adpsend2: Long,
                val convCnt: Long,
                val convCa: Long,
                var roas: Double,
            )

            Then("테스트파일 생성") {

                val data1 = file1.readCsvLines().fromLines<RptData>()
                log.info { "데이터1 : ${data1.size}건" }
                val data2 = file2.readCsvLines().fromLines<RptData>()
                log.info { "데이터2 : ${data2.size}건" }

                val merged = (data1 + data2).groupBy { it.kwd.uppercase() }.entries.map { (kwd, list) ->
                    RptData(
                        kwd = kwd,
                        imp = list.sumOf { it.imp },
                        click = list.sumOf { it.click },
                        adpsend = list.sumOf { it.adpsend },
                        adpsend2 = list.sumOf { it.adpsend2 },
                        convCnt = list.sumOf { it.convCnt },
                        convCa = list.sumOf { it.convCa },
                        roas = 0.0,
                    ).also {
                        val toDouble = try {
                            (1.0 * it.convCa / it.adpsend2 * 100).toBigDecimal2().halfUp(2).toDouble()
                        } catch (e: Exception) {
                            0.0
                        }
                        it.roas = toDouble
                    }
                }.sortedByDescending { it.imp }

                Excel().also { xls ->
                    xls.createSheet("리포트").apply {
                        addHeader(Lists.newArrayList("키워드", "노출", "클릭", "광고비", "광고비(전환발생일)", "전환수", "매출", "ROAS"))
                        merged.forEach {
                            writeLine(arrayOf(it.kwd, it.imp, it.click, it.adpsend, it.adpsend2, it.convCnt, it.convCa, it.roas))
                        }

                    }
                    //래핑해주고 파일로 쓰기
                    xls.wrap().write(resultFile)
                }


            }


        }

    }

}