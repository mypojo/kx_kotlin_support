package net.kotlinx.aws.rds

import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.rdsdata.AwsDbClient
import net.kotlinx.collection.mapOf
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class AwsDbClientDmpTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile49) }

    init {
        initTest(KotestUtil.IGNORE)

        /** DB 세팅되면 테스트 만들기 */
        Given("AwsDbClient") {

            val db = AwsDbClient {
                this.aws = this@AwsDbClientDmpTest.aws
                this.resourceName = "main-prod"
                this.databaseName = "${findProfile49}-dev"
                this.secretManagerName = resourceName
            }

            Then("단순 조회 테스트") {
                val param = mapOf {
                    "id" to 11420002
                }
                val res = db.executeStatement("select * from dmp.member where member_id = :id limit 10", param)
                res.records!!.forEach { t ->
                    log.debug { t.joinToString("\t") }
                }
            }
        }
    }

}
