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
        initTest(KotestUtil.PROJECT)

        Given("AwsDbClient") {

            val db = AwsDbClient {
                this.aws = this@AwsDbClientDmpTest.aws
                //aurora-postgresql-dev.cluster-cbsovqqs2kr2.ap-northeast-2.rds.amazonaws.com
                this.resourceName = "aurora-xxx"
                this.databaseName = "xx"
                this.secretManagerName = "rds!xxx"
            }

            Then("단순 조회 테스트") {
                val param = mapOf {
                    "id" to 1
                }
                val res = db.executeStatement("select * from member where 1 = :id limit 10", param)
                res.records!!.forEach { t ->
                    log.debug { t.joinToString("\t") }
                }
            }
        }
    }

}
