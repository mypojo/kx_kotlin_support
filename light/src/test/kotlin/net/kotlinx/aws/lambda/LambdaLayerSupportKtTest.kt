package net.kotlinx.aws.lambda

import net.kotlinx.aws.AwsClient1
import net.kotlinx.koin.Koins.koin
import net.kotlinx.kotest.BeSpecLight
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest

class LambdaLayerSupportKtTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        val aws = koin<AwsClient1>()

        Given("lambda") {
            Then("람다의 레이어 정보 업데이트") {
                aws.lambda.updateFunctionLayers("kx-fn-dev", listOf("arn:aws:lambda:ap-northeast-2:519433147926:layer:layer-kx-fn-dev:6"))
            }
        }
    }

}