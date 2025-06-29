package net.kotlinx.aws.bedrock

import aws.sdk.kotlin.services.bedrockagentruntime.BedrockAgentRuntimeClient
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist

/** 배드락 에이전트 추론 관련 클라이언트 */
val AwsClient.brar: BedrockAgentRuntimeClient
    get() = getOrCreateClient { BedrockAgentRuntimeClient { awsConfig.build(this) }.regist(awsConfig) }



