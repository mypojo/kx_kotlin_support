package net.kotlinx.aws.lambda

import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy

class LambdaLayerSupportKtTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("lambda") {
            val aws by koinLazy<AwsClient>()
            Then("람다의 레이어 정보 업데이트") {
                aws.lambda.updateFunctionLayers("kx-fn-dev", listOf("arn:aws:lambda:ap-northeast-2:xxx:layer:layer-kx-fn-dev:6"))
            }
        }
    }

}