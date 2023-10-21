package net.kotlinx.aws.logs

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient
import net.kotlinx.core.test.TestLevel03
import net.kotlinx.core.test.TestRoot

class CloudWatchLogsSupportKt_로그삭제 : TestRoot() {

    val aws = AwsConfig().toAwsClient()

    @TestLevel03
    fun test() {

        runBlocking {
            aws.logs.cleanLogStream("/aws/lambda/kx-fn-dev")
        }

    }

}