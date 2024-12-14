package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockruntime.BedrockRuntimeClient
import aws.sdk.kotlin.services.bedrockruntime.invokeModel
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

/** 배드락 추론 관련 클라이언트 */
val AwsClient.brr: BedrockRuntimeClient
    get() = getOrCreateClient { BedrockRuntimeClient { awsConfig.build(this) }.regist(awsConfig) }


/** 
 * agent 아닌, 일반 모델 실행
 * */
suspend fun BedrockRuntimeClient.invokeModel(modelId: String, body: Any): BedrockResult {
    val resp = this.invokeModel {
        this.modelId = modelId
        this.contentType = "application/json" //json으로 통일
        this.accept = "application/json" //json으로 통일
        this.body = body.toString().toByteArray()
    }
    return BedrockResult(resp)
}
