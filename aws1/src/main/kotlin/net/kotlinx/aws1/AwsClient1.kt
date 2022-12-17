package net.kotlinx.aws1

import aws.sdk.kotlin.services.s3.S3Client

/**
 * 기본 AWS 설정
 * http 엔진 설정이 빠져있는데 문제시 적당한거 넣기
 *  */
open class AwsClient1(private val awsConfig: AwsConfig) {

    //==================================================== 클라이언트 설정 ======================================================
    val s3: S3Client by lazy { S3Client { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider } }
}

fun AwsConfig.toAwsClient():AwsClient1 = AwsClient1(this)