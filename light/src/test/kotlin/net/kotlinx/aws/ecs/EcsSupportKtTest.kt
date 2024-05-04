package net.kotlinx.aws.ecs

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsClient1
import net.kotlinx.test.MyAws1Module
import net.kotlinx.test.TestLight
import org.junit.jupiter.api.Test
import org.koin.core.component.get

class EcsSupportKtTest : TestLight() {


    @Test
    fun updateServiceCount() {
        MyAws1Module.PROFILE_NAME = "sin"
        runBlocking {
            val aws = get<AwsClient1>()
            aws.ecs.updateServiceCount("${MyAws1Module.PROFILE_NAME}-web_cluster-dev", "${MyAws1Module.PROFILE_NAME}-web_service-dev", 1)
        }

    }
}