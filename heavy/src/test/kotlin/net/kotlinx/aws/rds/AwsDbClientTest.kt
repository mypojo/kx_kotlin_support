package net.kotlinx.aws.rds

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class AwsDbClientTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("AwsDbClient") {

            val db = AwsDbClient {
                this.aws = this@AwsDbClientTest.aws
                this.resourceName = "main-prod"
                this.databaseName = "${findProfile97}_prod"
                this.secretManagerName = "main-prod-HsmRNg"
            }

            Then("단순 조회 테스트") {
                val res = db.executeStatement("select * from media_report limit 3", emptyMap<String, Any>())
                res.records!!.forEach { t ->
                    log.debug { t.joinToString("\t") }
                }
            }
        }
    }

}
