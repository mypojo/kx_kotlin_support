package net.kotlinx.aws

import aws.sdk.kotlin.services.codecommit.CodeCommitClient
import aws.sdk.kotlin.services.codedeploy.CodeDeployClient
import aws.sdk.kotlin.services.costexplorer.CostExplorerClient
import aws.sdk.kotlin.services.ec2.Ec2Client
import aws.sdk.kotlin.services.ecr.EcrClient
import aws.sdk.kotlin.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client
import aws.sdk.kotlin.services.eventbridge.EventBridgeClient
import aws.sdk.kotlin.services.iam.IamClient
import aws.sdk.kotlin.services.rds.RdsClient
import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.sqs.SqsClient
import net.kotlinx.aws.iam.IamSecretUpdateModule
import net.kotlinx.aws.sm.SmStore

/** 선형 구조임으로 조합보다는 상속이 더 좋은 선택 */
class AwsClient(awsConfig: AwsConfig) : AwsClient1(awsConfig) {
    //==================================================== 공통 ======================================================
    val iam: IamClient by lazy { IamClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }
    val cost: CostExplorerClient by lazy { CostExplorerClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }
    val ses: SesClient by lazy { SesClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }
    val elb: ElasticLoadBalancingV2Client by lazy { ElasticLoadBalancingV2Client  { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }

    //==================================================== 저장소 ======================================================
    val rds: RdsClient by lazy { RdsClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }
    val sqs: SqsClient by lazy { SqsClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }
    val sm: SecretsManagerClient by lazy { SecretsManagerClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }
    val event: EventBridgeClient by lazy { EventBridgeClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }

    //==================================================== 컴퓨팅 인프라 ======================================================
    val ec2: Ec2Client by lazy { Ec2Client { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }

    //==================================================== 코드 시리즈 ======================================================
    val codeDeploy: CodeDeployClient by lazy { CodeDeployClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }
    val codeCommit: CodeCommitClient by lazy { CodeCommitClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }
    val ecr: EcrClient by lazy { EcrClient { region = awsConfig.region; credentialsProvider = awsConfig.credentialsProvider; httpClient = awsConfig.httpClientEngine; } }

    //==================================================== 모듈 설정 ======================================================
    val iamSecretUpdateModule: IamSecretUpdateModule by lazy { IamSecretUpdateModule(iam) }

    //==================================================== 기타 ======================================================
    /** 시크릿 매니저 스토어 */
    val smStore: SmStore by lazy { SmStore(sm) }

}

fun AwsConfig.toAwsClient(): AwsClient = AwsClient(this)