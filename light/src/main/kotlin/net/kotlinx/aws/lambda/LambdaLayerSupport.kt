package net.kotlinx.aws.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.publishLayerVersion
import aws.sdk.kotlin.services.lambda.updateFunctionConfiguration

/** 레이어 업데이트 될때마다 버전이 올라가니 주의할것!  */
suspend fun LambdaClient.publishLayerVersion(bucket: String, key: String, layerName: String): String {
    val layer = this.publishLayerVersion {
        this.layerName = layerName
        this.compatibleRuntimes = listOf(aws.sdk.kotlin.services.lambda.model.Runtime.Java11)
        this.content {
            this.s3Key = key
            this.s3Bucket = bucket
        }
    }
    return layer.layerVersionArn!!
}

/**
 * 특정 함수에 특정 레이어의 최신 버전을 적용시켜준다.
 * 주의!! 람다 레이어는 람다 버전별로 다르게 붙을 수 있음. 그냥 최신버전에 달아주고, 람다 버전을 올리도록 하자.
 * @param layerVersionArns 버전이 담긴 arn이 와야 함 주의!!
 *  */
suspend fun LambdaClient.updateFunctionLayers(funtionName: String, layerVersionArns: List<String>) {
    this.updateFunctionConfiguration {
        this.functionName = funtionName
        this.layers = layerVersionArns
    }
}