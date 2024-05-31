package net.kotlinx.aws

import aws.smithy.kotlin.runtime.client.SdkClient


/**
 * SdkClient 내부에서도 AwsConfig 로 직접 접근 가능하게 추가
 * */
private val CLIENT_CONFIG: MutableMap<SdkClient, AwsConfig> = mutableMapOf()

fun <T : SdkClient> T.regist(awsConfig: AwsConfig): T {
    CLIENT_CONFIG[this] = awsConfig
    return this
}

val SdkClient.awsConfig: AwsConfig
    get() = CLIENT_CONFIG[this]!!