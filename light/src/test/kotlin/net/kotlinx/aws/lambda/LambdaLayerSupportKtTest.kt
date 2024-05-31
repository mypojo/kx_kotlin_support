package net.kotlinx.aws.lambda

import net.kotlinx.aws.AwsClient1
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight

class LambdaLayerSupportKtTest : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("lambda") {
            val aws by koinLazy<AwsClient1>()
            Then("람다의 레이어 정보 업데이트") {
                aws.lambda.updateFunctionLayers("kx-fn-dev", listOf("arn:aws:lambda:ap-northeast-2:xxx:layer:layer-kx-fn-dev:6"))
            }
        }
    }

}