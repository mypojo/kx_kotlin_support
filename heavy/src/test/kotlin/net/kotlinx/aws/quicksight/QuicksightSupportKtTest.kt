package net.kotlinx.aws.quicksight

import aws.sdk.kotlin.services.quicksight.model.IngestionType
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.athena.AthenaUtil
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.regex.RegexSet
import net.kotlinx.regex.retainFrom
import net.kotlinx.system.DeploymentType

class QuicksightSupportKtTest : BeSpecHeavy() {

    private val client by lazy { koin<AwsClient>(findProfile97) }

    val suff = DeploymentType.DEV.suff

    //val suff = DeploymentType.PROD.suff
    val dbName = "${suff.subSequence(0, 1)}3"


    init {
        initTest(KotestUtil.IGNORE)

        Given("퀵사이트 분석") {

            Then("데이터세트 리스팅") {
                val items = client.quicksight.listDataSets()
                items.printSimple()
            }

            Then("데이터세트 상세") {
                val items = client.quicksight.describeDataSet("nv_rpt_kwd-dev-demo")
                println(items)
            }

            val dataSourceAthena = "athena-${suff}"
            val tableName = "nv_rpt_kwd"
            val datasetId = "${tableName}-${suff}-demo"
            Then("데이터세트 만들기") {
                val columns = athenaModule97.readAll { AthenaUtil.schemaColumn(dbName, tableName) }
                val setConfig = QuicksightDataSetConfig {
                    this.dataSourceId = dataSourceAthena
                    this.dataSetId = datasetId
                    this.dataSetName = "리포트_네이버_검색광고_키워드-${suff}-demo"
                    this.users = listOf("DEV/sin")
                    this.folderIds = listOf("rpt_naver")
                    this.columns = QuicksightUtil.toColumnMap(columns.drop(1).map { it[1] to it[2].retainFrom(RegexSet.ALPAH_NUMERIC.ALPA) })
                    this.schema = dbName
                    this.tableName = tableName
                    this.rowLevelPermissionDataSet = "role_user_adv-dev"
                }
                client.quicksight.createDataSet(setConfig)
                client.quicksight.putDataSetRefreshProperties(datasetId, "date", 5)
            }

            Then("데이터세트 리프레시") {
                client.quicksight.refreshDataSet(datasetId, IngestionType.FullRefresh)
            }

        }
    }

}
