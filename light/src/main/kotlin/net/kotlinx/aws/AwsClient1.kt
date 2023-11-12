package net.kotlinx.aws

import aws.sdk.kotlin.services.athena.AthenaClient
import aws.sdk.kotlin.services.batch.BatchClient
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.ecs.EcsClient
import aws.sdk.kotlin.services.firehose.FirehoseClient
import aws.sdk.kotlin.services.kinesis.KinesisClient
import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.sfn.SfnClient
import aws.sdk.kotlin.services.ssm.SsmClient
import net.kotlinx.aws.ssm.SsmStore

/**
 * 기본 AWS 설정
 * http 엔진 설정이 빠져있는데 문제시 적당한거 넣기
 * 중요!!! http 커넥션에서 use 키워드 의미없어보임. 그냥 람다 코루틴에서 오류나는건 리트라이 하자.
 *  */
open class AwsClient1(val awsConfig: AwsConfig) {

    //==================================================== 클라이언트 설정 ======================================================
    val s3: S3Client by lazy { S3Client { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }
    val kinesis: KinesisClient by lazy { KinesisClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }
    val firehose: FirehoseClient by lazy { FirehoseClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }
    val dynamo: DynamoDbClient by lazy { DynamoDbClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }
    val lambda: LambdaClient by lazy { LambdaClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }

    //==================================================== 컴퓨팅 인프라 ======================================================
    val sfn: SfnClient by lazy { SfnClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }
    val athena: AthenaClient by lazy { AthenaClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }
    val batch: BatchClient by lazy { BatchClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }
    val ecs: EcsClient by lazy { EcsClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }

    /** 저장소 */
    val ssm: SsmClient by lazy { SsmClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }

    //==================================================== 기타 ======================================================
    /** SSM(Systems Manager) 스토어. = 파라메터 스토어 */
    val ssmStore: SsmStore by lazy { SsmStore(ssm) }
}