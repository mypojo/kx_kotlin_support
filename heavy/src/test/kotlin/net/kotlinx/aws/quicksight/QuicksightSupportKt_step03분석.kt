package net.kotlinx.aws.quicksight

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class QuicksightSupportKt_step03분석 : BeSpecHeavy() {

    private val client by koinLazy<AwsClient>(findProfile49)

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

        }
    }

}
