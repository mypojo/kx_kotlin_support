package net.kotlinx.aws

import aws.sdk.kotlin.services.athena.AthenaClient
import aws.sdk.kotlin.services.batch.BatchClient
import aws.sdk.kotlin.services.ecr.EcrClient
import aws.sdk.kotlin.services.iam.IamClient
import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.sdk.kotlin.services.sfn.SfnClient
import aws.sdk.kotlin.services.ssm.SsmClient
import net.kotlinx.aws.iam.IamSecretUpdateModule
import net.kotlinx.aws.ssm.SsmStore
import net.kotlinx.aws1.AwsClient1
import net.kotlinx.aws1.AwsConfig

/** 선형 구조임으로 조합보다는 상속이 더 좋은 선택 */
class AwsClient(val awsConfig: AwsConfig) : AwsClient1(awsConfig) {
    //==================================================== 클라이언트 설정 ======================================================
    val lambda: LambdaClient by lazy { LambdaClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClientEngine = awsConfig.httpClientEngine; } }
    val iam: IamClient by lazy { IamClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClientEngine = awsConfig.httpClientEngine; } }
    val ssm: SsmClient by lazy { SsmClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClientEngine = awsConfig.httpClientEngine; } }
    val sm: SecretsManagerClient by lazy { SecretsManagerClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClientEngine = awsConfig.httpClientEngine; } }
    val athena: AthenaClient by lazy { AthenaClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClientEngine = awsConfig.httpClientEngine; } }
    val batch: BatchClient by lazy { BatchClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClientEngine = awsConfig.httpClientEngine; } }
    val sfn: SfnClient by lazy { SfnClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClientEngine = awsConfig.httpClientEngine; } }
    val ecr: EcrClient by lazy { EcrClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClientEngine = awsConfig.httpClientEngine; } }

    //==================================================== 모듈 설정 ======================================================
    val iamSecretUpdateModule: IamSecretUpdateModule by lazy { IamSecretUpdateModule(iam) }

    //==================================================== 기타 ======================================================
    val ssmStore: SsmStore by lazy { SsmStore(ssm) }

}

fun AwsConfig.toAwsClient(): AwsClient = AwsClient(this)