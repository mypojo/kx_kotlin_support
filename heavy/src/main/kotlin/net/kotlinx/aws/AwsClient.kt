package net.kotlinx.aws

import aws.sdk.kotlin.services.budgets.BudgetsClient
import aws.sdk.kotlin.services.codecommit.CodeCommitClient
import aws.sdk.kotlin.services.codedeploy.CodeDeployClient
import aws.sdk.kotlin.services.costexplorer.CostExplorerClient
import aws.sdk.kotlin.services.ec2.Ec2Client
import aws.sdk.kotlin.services.ecr.EcrClient
import aws.sdk.kotlin.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client
import aws.sdk.kotlin.services.eventbridge.EventBridgeClient
import aws.sdk.kotlin.services.glue.GlueClient
import aws.sdk.kotlin.services.iam.IamClient
import aws.sdk.kotlin.services.lakeformation.LakeFormationClient
import aws.sdk.kotlin.services.rds.RdsClient
import aws.sdk.kotlin.services.scheduler.SchedulerClient
import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.sqs.SqsClient
import net.kotlinx.aws.iam.IamSecretUpdateModule
import net.kotlinx.aws.sm.SmStore

/** 선형 구조임으로 조합보다는 상속이 더 좋은 선택 */
class AwsClient(awsConfig: AwsConfig) : AwsClient1(awsConfig) {
    //==================================================== 공통 ======================================================
    val iam: IamClient by lazy { IamClient { awsConfig.build(this) }.regist(awsConfig) }
    val cost: CostExplorerClient by lazy { CostExplorerClient { awsConfig.build(this) }.regist(awsConfig) }
    val budget: BudgetsClient by lazy { BudgetsClient { awsConfig.build(this) }.regist(awsConfig) }
    val ses: SesClient by lazy { SesClient { awsConfig.build(this) }.regist(awsConfig) }
    val elb: ElasticLoadBalancingV2Client by lazy { ElasticLoadBalancingV2Client { awsConfig.build(this) }.regist(awsConfig) }
    val lake: LakeFormationClient by lazy { LakeFormationClient { awsConfig.build(this) }.regist(awsConfig) }
    val glue: GlueClient by lazy { GlueClient { awsConfig.build(this) }.regist(awsConfig) }

    //==================================================== 저장소 ======================================================
    val rds: RdsClient by lazy { RdsClient { awsConfig.build(this) }.regist(awsConfig) }
    val sqs: SqsClient by lazy { SqsClient { awsConfig.build(this) }.regist(awsConfig) }
    val sm: SecretsManagerClient by lazy { SecretsManagerClient { awsConfig.build(this) }.regist(awsConfig) }
    val event: EventBridgeClient by lazy { EventBridgeClient { awsConfig.build(this) }.regist(awsConfig) }
    val schedule: SchedulerClient by lazy { SchedulerClient { awsConfig.build(this) }.regist(awsConfig) }

    //==================================================== 컴퓨팅 인프라 ======================================================
    val ec2: Ec2Client by lazy { Ec2Client { awsConfig.build(this) }.regist(awsConfig) }

    //==================================================== 코드 시리즈 ======================================================
    val codeDeploy: CodeDeployClient by lazy { CodeDeployClient { awsConfig.build(this) }.regist(awsConfig) }
    val codeCommit: CodeCommitClient by lazy { CodeCommitClient { awsConfig.build(this) }.regist(awsConfig) }
    val ecr: EcrClient by lazy { EcrClient { awsConfig.build(this) }.regist(awsConfig) }

    //==================================================== 모듈 설정 ======================================================
    val iamSecretUpdateModule: IamSecretUpdateModule by lazy { IamSecretUpdateModule(iam) }

    //==================================================== 기타 ======================================================
    /** 시크릿 매니저 스토어 */
    val smStore: SmStore by lazy { SmStore(sm) }

}

fun AwsConfig.toAwsClient(): AwsClient = AwsClient(this)