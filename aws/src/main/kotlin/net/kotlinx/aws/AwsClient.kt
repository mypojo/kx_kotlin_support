package net.kotlinx.aws

import aws.sdk.kotlin.services.lambda.LambdaClient
import net.kotlinx.aws1.AwsClient1
import net.kotlinx.aws1.AwsConfig

/** 선형 구조임으로 조합보다는 상속이 더 좋은 선택 */
class AwsClient(private val awsConfig: AwsConfig) : AwsClient1(awsConfig) {
    //==================================================== 클라이언트 설정 ======================================================
    val lambda: LambdaClient by lazy { LambdaClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider } }
}

fun AwsConfig.toAwsClient():AwsClient = AwsClient(this)