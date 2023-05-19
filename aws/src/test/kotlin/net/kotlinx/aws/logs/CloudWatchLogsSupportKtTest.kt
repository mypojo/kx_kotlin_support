package net.kotlinx.aws.logs

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.toAwsClient
import net.kotlinx.aws1.AwsConfig
import net.kotlinx.core2.test.TestLevel03
import net.kotlinx.core2.test.TestRoot

class CloudWatchLogsSupportKtTest : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient()

    @TestLevel03
    fun test() {

        runBlocking {
            aws.logs.cleanLogStream("/aws/lambda/sin-batchFunction-dev")
        }

    }

}