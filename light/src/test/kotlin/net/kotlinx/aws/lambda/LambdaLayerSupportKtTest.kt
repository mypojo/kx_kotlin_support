package net.kotlinx.aws.lambda

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient1
import net.kotlinx.test.TestRoot
import org.junit.jupiter.api.Test

class LambdaLayerSupportKtTest : TestRoot(){

    val aws = AwsConfig().toAwsClient1()

    @Test
    fun test() {

        runBlocking {
            aws.lambda.updateFunctionLayers("kx-fn-dev", listOf("arn:aws:lambda:ap-northeast-2:519433147926:layer:layer-kx-fn-dev:6"))
        }

    }

}