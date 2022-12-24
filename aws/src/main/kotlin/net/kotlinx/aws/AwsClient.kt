package net.kotlinx.aws

import aws.sdk.kotlin.services.iam.IamClient
import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.sdk.kotlin.services.ssm.SsmClient
import net.kotlinx.aws.module.AwsIamModule
import net.kotlinx.aws.module.AwsSsmStore
import net.kotlinx.aws1.AwsClient1
import net.kotlinx.aws1.AwsConfig

/** 선형 구조임으로 조합보다는 상속이 더 좋은 선택 */
class AwsClient(private val awsConfig: AwsConfig) : AwsClient1(awsConfig) {
    //==================================================== 클라이언트 설정 ======================================================
    val lambda: LambdaClient by lazy { LambdaClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider } }
    val iam: IamClient by lazy { IamClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider } }
    val ssm: SsmClient by lazy { SsmClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider } }
    val sm: SecretsManagerClient by lazy { SecretsManagerClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider } }

    //==================================================== 모듈 설정 ======================================================
    val iamModule: AwsIamModule by lazy { AwsIamModule(iam) }

    //==================================================== 기타 ======================================================
    val ssmStore: AwsSsmStore by lazy { AwsSsmStore(ssm) }

}

fun AwsConfig.toAwsClient(): AwsClient = AwsClient(this)