package net.kotlinx.aws.logs

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient
import net.kotlinx.test.TestLevel03
import net.kotlinx.test.TestRoot

class CloudWatchLogsSupportKt_로그삭제 : TestRoot() {

    val projectName = "sin"

    val aws = AwsConfig(projectName).toAwsClient()

    @TestLevel03
    fun test() {

        runBlocking {
            aws.logs.cleanLogStream("/aws/lambda/${projectName}-fn-dev")
        }

    }

}