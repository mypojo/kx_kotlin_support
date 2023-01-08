package net.kotlinx.aws1

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.firehose.FirehoseClient
import aws.sdk.kotlin.services.kinesis.KinesisClient
import aws.sdk.kotlin.services.s3.S3Client

/**
 * 기본 AWS 설정
 * http 엔진 설정이 빠져있는데 문제시 적당한거 넣기
 *  */
open class AwsClient1(private val awsConfig: AwsConfig) {

    //==================================================== 클라이언트 설정 ======================================================
    val s3: S3Client by lazy { S3Client { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClientEngine =  awsConfig.httpClientEngine; } }
    val kinesis: KinesisClient by lazy { KinesisClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClientEngine = awsConfig.httpClientEngine; } }
    val firehose: FirehoseClient by lazy { FirehoseClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClientEngine = awsConfig.httpClientEngine; } }
    val dynamo: DynamoDbClient by lazy { DynamoDbClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClientEngine = awsConfig.httpClientEngine; } }
}

/** 간단 변환 */
fun AwsConfig.toAwsClient1(): AwsClient1 = AwsClient1(this)