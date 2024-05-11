package net.kotlinx.aws.ecs

import net.kotlinx.aws.AwsClient1
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.kotest.modules.MyAws1Module

class EcsSupportKtTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("ecs") {
            val aws = koin<AwsClient1>()
            Then("updateServiceCount - ECS 서비스 터치") {
                aws.ecs.updateServiceCount("${MyAws1Module.PROFILE_NAME}-web_cluster-dev", "${MyAws1Module.PROFILE_NAME}-web_service-dev", 1)
            }
        }
    }
}