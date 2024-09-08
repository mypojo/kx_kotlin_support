package net.kotlinx.aws.glue

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class GlueSupportKtTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile97) }

    init {
        initTest(KotestUtil.FAST)

        Given("글루") {
            Then("데이터베이스 생성") {
                aws.glue.createDatabase("demo1")
            }
        }
    }

}
