package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrock.BedrockClient
import aws.sdk.kotlin.services.bedrock.model.ListFoundationModelsResponse
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist
import net.kotlinx.string.toTextGridPrint

/** 배드락 모델 관련 클라이언트 */
val AwsClient.br: BedrockClient
    get() = getOrCreateClient { BedrockClient { awsConfig.build(this) }.regist(awsConfig) }

fun ListFoundationModelsResponse.printSimple() {
    listOf("modelId", "modelName", "inputModalities", "inferenceTypesSupported", "outputModalities", "providerName").toTextGridPrint {
        this.modelSummaries!!.map { model ->
            arrayOf(
                model.modelId,
                model.modelName,
                model.inputModalities?.joinToString(","),
                model.inferenceTypesSupported?.joinToString(","),
                model.outputModalities?.joinToString(","),
                model.providerName
            )
        }
    }
}






