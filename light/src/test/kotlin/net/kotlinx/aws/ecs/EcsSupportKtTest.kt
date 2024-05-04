package net.kotlinx.aws.ecs

import net.kotlinx.aws.AwsClient1
import net.kotlinx.kotest.BeSpecLight
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.test.MyAws1Module
import org.koin.core.component.get

class EcsSupportKtTest : BeSpecLight() {

    init {
        initTest(KotestUtil.FAST)

        Given("ecs") {
            val aws = get<AwsClient1>()
            Then("updateServiceCount") {
                aws.ecs.updateServiceCount("${MyAws1Module.PROFILE_NAME}-web_cluster-dev", "${MyAws1Module.PROFILE_NAME}-web_service-dev", 1)
            }
        }
    }
}