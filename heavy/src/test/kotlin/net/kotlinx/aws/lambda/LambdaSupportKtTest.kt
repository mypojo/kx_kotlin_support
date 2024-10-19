package net.kotlinx.aws.lambda

import aws.sdk.kotlin.services.lambda.listFunctions
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.toLocalDateTime
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.string.toTextGridPrints
import net.kotlinx.time.toKr01

internal class LambdaSupportKtTest : BeSpecHeavy() {

    private val aws by lazy { koin<AwsClient>(findProfile28) }

    init {
        initTest(KotestUtil.PROJECT)

        Given("LambdaSupportKt") {

            Then("함수 리스팅") {
                listOf("함수명", "코드사이즈", "ARN").toTextGridPrints {
                    aws.lambda.listFunctions { maxItems = 10 }.functions!!.map {
                        arrayOf(it.functionName, it.codeSize, it.functionArn)
                    }
                }
            }

            Then("레이어 최신버전 확인") {
                val layerNames = listOf(
                    "$findProfile28-layer_v1-dev",
                    "$findProfile28-layer_v2-dev",
                    "$findProfile28-layer_v3-dev",
                )
                val layers = aws.lambda.listLayerVersions(layerNames)

                listOf("ARN", "생성일자").toTextGridPrint {
                    layers.map { arrayOf(it.layerVersionArn, it.createdDate!!.toLocalDateTime().toKr01()) }
                }
            }

            xThen("업데이트") {
                aws.lambda.updateFunctionCode("sin-job_lambda-prod", "463327615611.dkr.ecr.ap-northeast-2.amazonaws.com/sin-job", "local-2023-03-22_12-04")
            }
            xThen("버전업&교체") {
                //aws.lambda.publishVersionAndUpdateAlias("sin-batchFunction-dev", LambdaUtil.SERVICE_ON)
            }
        }
    }

}