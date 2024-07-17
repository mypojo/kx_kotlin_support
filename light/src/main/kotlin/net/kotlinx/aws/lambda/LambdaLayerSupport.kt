package net.kotlinx.aws.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.listLayerVersions
import aws.sdk.kotlin.services.lambda.model.LayerVersionsListItem
import aws.sdk.kotlin.services.lambda.publishLayerVersion
import aws.sdk.kotlin.services.lambda.updateFunctionConfiguration

/** 레이어 업데이트 될때마다 버전이 올라가니 주의할것!  */
suspend fun LambdaClient.publishLayerVersion(
    bucket: String,
    key: String,
    layerName: String,
    runtimes: List<aws.sdk.kotlin.services.lambda.model.Runtime> = listOf(aws.sdk.kotlin.services.lambda.model.Runtime.Java21)
): String {
    val layer = this.publishLayerVersion {
        this.layerName = layerName
        this.compatibleRuntimes = runtimes
        this.content {
            this.s3Key = key
            this.s3Bucket = bucket
        }
    }
    return layer.layerVersionArn!!
}

/**
 * 레이어 이름에 대해서, 각 최신 레이어 ARN을 얻어온다.
 * updateFunctionLayers 호출할 용도로 사용됨
 *  */
suspend fun LambdaClient.listLayerVersions(layerNames: List<String>): List<LayerVersionsListItem> {
    return layerNames.map { layerName ->
        this.listLayerVersions {
            this.layerName = layerName
            this.maxItems = 1
        }.layerVersions!!.first()
    }
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