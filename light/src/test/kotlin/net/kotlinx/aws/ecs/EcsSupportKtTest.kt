package net.kotlinx.aws.ecs

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsClient1
import net.kotlinx.test.MyLightKoinStarter
import net.kotlinx.test.TestLight
import org.junit.jupiter.api.Test
import org.koin.core.component.get

class EcsSupportKtTest : TestLight() {


    companion object {
        const val PROFILE = "sin"

        init {
            MyLightKoinStarter.startup(PROFILE)
        }
    }

    @Test
    fun updateServiceCount() {
        runBlocking {
            val aws = get<AwsClient1>()
            aws.ecs.updateServiceCount("${PROFILE}-web_cluster-dev", "${PROFILE}-web_service-dev", 1)
        }

    }
}