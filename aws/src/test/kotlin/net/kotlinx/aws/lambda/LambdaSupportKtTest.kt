package net.kotlinx.aws.lambda

import io.kotest.common.runBlocking
import net.kotlinx.TestRoot
import net.kotlinx.aws.toAwsClient
import net.kotlinx.aws1.AwsConfig
import org.junit.jupiter.api.Test

internal class LambdaSupportKtTest : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient()

    @Test
    fun `기본테스트`() {

        runBlocking {
            aws.lambda.updateFunctionCode("sin-job_lambda-prod", "463327615611.dkr.ecr.ap-northeast-2.amazonaws.com/sin-job", "local-2023-03-22_12-04")
        }

    }

    fun main(args: Array<String>) {
        //aws.lambda.updateFunctionCode("sin-job_lambda-prod", "463327615611.dkr.ecr.ap-northeast-2.amazonaws.com/sin-job:local-2023-03-22_12-04")
    }
}