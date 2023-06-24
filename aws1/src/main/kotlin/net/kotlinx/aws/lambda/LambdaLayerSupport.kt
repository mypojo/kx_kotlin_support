package net.kotlinx.aws.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.publishLayerVersion
import aws.sdk.kotlin.services.lambda.updateFunctionConfiguration

/** 레이어 업데이트 될때마다 버전이 올라가니 주의할것!  */
suspend fun LambdaClient.publishLayerVersion(bucket: String, key: String, layerName: String) {
    val layer = this.publishLayerVersion {
        this.layerName = layerName
        this.compatibleRuntimes = listOf(aws.sdk.kotlin.services.lambda.model.Runtime.Java11)
        this.content {
            this.s3Key = key
            this.s3Bucket = bucket
        }
    }
}

/** 특정 함수에 특정 레이어의 최신 버전을 적용시켜준다  */
suspend fun LambdaClient.updateFunctionLayers(funtionName: String, vararg layerNames: String) {
    val layerArns = this.listLayers().layers!!.filter { it.layerName in layerNames.toSet() }.map { it.layerArn!! }
    this.updateFunctionConfiguration {
        this.functionName = funtionName
        this.layers = layerArns
    }
}