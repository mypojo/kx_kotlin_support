package net.kotlinx.aws.glue

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print

class GlueSupportKtTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.IGNORE)

        Given("글루") {
            Then("데이터베이스 생성") {
                aws.glue.createDatabase("demo1")
            }

            Then("테이블 정보") {
                val table = aws.glue.getTable("d1", "http_log")
                listOf(table).print()
                listOf(table.storageDescriptor!!).print()
                log.info { "저장소 : ${table.storageDescriptor!!.location}" }
            }
        }

        Given("데이터 레이크") {
            Then("데이터베이스 생성") {
                //aws.glue.createOrUpdateTableOptimizerByDefault("d1", "http_log", "app-glue")
                aws.glue.createOrUpdateTableOptimizerByDefault("p1", "http_log", "app-glue")
            }
        }
    }

}
