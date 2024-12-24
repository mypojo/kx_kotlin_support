package net.kotlinx.aws.quicksight

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class QuicksightSupportKt_초기화 : BeSpecHeavy() {

    private val client by koinLazy<AwsClient>(findProfile97)


    init {
        initTest(KotestUtil.IGNORE)

        Given("최초 생성된 데모 삭제하기") {

            Then("분석 리스팅") {
                val items = client.quicksight.listAnalyses()
                items.printSimple()
            }

            xThen("분석 삭제") {
                val items = client.quicksight.listAnalyses()
                items.forEach { t -> client.quicksight.deleteAnalysis(t.analysisId!!) }
                log.warn { "분석 ${items.size}건 삭제 완료" }
            }

            Then("데이터세트 리스팅") {
                val items = client.quicksight.listDataSets()
                items.printSimple()
            }

            Then("데이터세트 삭제") {
                val items = client.quicksight.listDataSets()
                items.printSimple()
                items.filter { it.name in setOf("People Overview", "Web and Social Media Analytics", "Business Review", "Sales Pipeline") }.forEach {
                    client.quicksight.deleteDataSet(it.dataSetId!!)
                    log.warn { "데이터세트 ${it.name} 삭제 완료" }
                }
            }

            Then("데이터소스 리스팅") {
                val items = client.quicksight.listDataSources()
                items.printSimple()
            }

            xThen("데이터소스 삭제") {
                val items = client.quicksight.listDataSources()
                items.forEach { t -> client.quicksight.deleteDataSourceIfExist(t.dataSourceId!!) }
                log.warn { "데이터세트 ${items.size}건 삭제 완료" }
            }
        }
    }

}
