package net.kotlinx.aws.lambda

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient
import net.kotlinx.kotest.BeSpecLog
import org.junit.jupiter.api.Test

internal class LambdaSupportKtTest : BeSpecLog(){
    init {
        val aws = AwsConfig(profileName = "sin").toAwsClient()
        val functionName = "sin-chatgpt-prod"

        @Test
        fun `레이어 - 리스팅`() {
            runBlocking {
                val versions = aws.lambda.listLayerVersions(
                    listOf(
                        "sin-layer_v1-dev",
                        "sin-layer_v2-dev",
                        "sin-layer_v3-dev",
                    )
                )
                versions.map { it.layerVersionArn }.forEach { println(it) }
            }
        }

        @Test
        fun `기본테스트`() {
            runBlocking {
                aws.lambda.updateFunctionCode("sin-job_lambda-prod", "463327615611.dkr.ecr.ap-northeast-2.amazonaws.com/sin-job", "local-2023-03-22_12-04")
            }
        }

        @Test
        fun `버전업&교체`() {
            runBlocking {
                aws.lambda.publishVersionAndUpdateAlias("sin-batchFunction-dev", LambdaUtil.SERVICE_ON)
            }
        }

        @Test
        fun `버전업`() {
            runBlocking {
                val resp = aws.lambda.publishVersion(functionName)
                println("업데이트된 버전 ${resp.version}")
            }
        }

        @Test
        fun `알리아스교체`() {
            runBlocking {
                val resp = aws.lambda.updateAlias(functionName, "3", "service-on")
                println("name ${resp.name}")
            }
        }

        @Test
        fun `알리아스생성`() {
            runBlocking {
                val resp = aws.lambda.createAlias(functionName, "3", "service-on")
                println("name ${resp.name}")
            }
        }
    }
}