package net.kotlinx.aws.quicksight

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class QuicksightSupportKt_step01데이터소스 : BeSpecHeavy() {

    private val client by koinLazy<AwsClient>(findProfile49)

    init {
        initTest(KotestUtil.IGNORE)

        Given("최초 생성된 데모 삭제하기") {

            Then("데이터소스 생성") {
                val ADMINS = listOf("DEV/sin")
                val items = client.quicksight.createDataSourceAthena(
                    id = "athena-dev",
                    sourceName = "athena-dev",
                    workGroup = "workgroup-dev",
                    users = ADMINS
                )
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
