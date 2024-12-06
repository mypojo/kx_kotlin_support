package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.listAnalyses
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class QuicksightSupportKtTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile28) }

    init {
        initTest(KotestUtil.IGNORE)

        Given("퀵사이트 분석") {

            val profile = findProfile28

            Then("분석 리스팅") {
                val analyses = aws.quicksight.listAnalyses {
                    awsAccountId = aws.awsConfig.awsId
                }
                println(analyses.analysisSummaryList)
            }

            xThen("데이터세트 전부 삭제") {
                aws.quicksight.listDataSets().forEach {
                    aws.quicksight.deleteDataSet(it.dataSetId!!)
                }
            }
        }
    }

}
