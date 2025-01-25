package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockruntime.BedrockRuntimeClient
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

/** 배드락 추론 관련 클라이언트 */
val AwsClient.brr: BedrockRuntimeClient
    get() = getOrCreateClient { BedrockRuntimeClient { awsConfig.build(this) }.regist(awsConfig) }



