package net.kotlinx.aws.ecs

import net.kotlinx.aws.AwsClient1
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class EcsSupportKtTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("ecs") {
            val aws by koinLazy<AwsClient1>()
            Then("updateServiceCount - ECS 서비스 터치") {
                aws.ecs.updateServiceCount("${""}-web_cluster-dev", "${""}-web_service-dev", 1)
            }
        }
    }
}