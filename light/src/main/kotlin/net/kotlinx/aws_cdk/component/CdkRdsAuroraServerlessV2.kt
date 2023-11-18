package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkInterface
import net.kotlinx.aws_cdk.toCdk
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.ec2.SubnetSelection
import software.amazon.awscdk.services.ec2.SubnetType
import software.amazon.awscdk.services.logs.RetentionDays
import software.amazon.awscdk.services.rds.*
import kotlin.time.Duration.Companion.days

/**
 * https://github.com/aws/aws-cdk/blob/main/packages/aws-cdk-lib/aws-rds/adr/aurora-serverless-v2.md
 *  */
class CdkRdsAuroraServerlessV2 : CdkInterface {

    @Kdsl
    constructor(block: CdkRdsAuroraServerlessV2.() -> Unit = {}) {
        apply(block)
    }

    override val logicalName: String
        get() = "${project.projectName}-rds_${name}"

    lateinit var vpc: IVpc
    lateinit var securityGroup: ISecurityGroup

    lateinit var name: String

    val maxConnection: Int = 300

    /** 인스턴스 수.  */
    var instanceCnt = 1

    /** 디폴트 7일 */
    var backupRetention = 7.days

    /** 새벽 2시~4시 */
    var backupWindow = "17:00-19:00"

    /** 최소 AU */
    var minCapacity = 0.5

    /** 최대 AU */
    var maxCapacity = 8

    fun create(stack: Stack) {

        val clusterEngine = DatabaseClusterEngine.auroraMysql(AuroraMysqlClusterEngineProps.builder().version(AuroraMysqlEngineVersion.VER_3_03_0).build())

        val parameterGroup = ParameterGroup(
            stack, "$logicalName-pg", ParameterGroupProps.builder()
                .engine(clusterEngine)
                .description("CDK - AuroraMysql - $logicalName")
                .parameters(
                    mapOf(
                        "time_zone" to "Asia/Seoul",
                        "transaction_isolation" to "READ-COMMITTED", //보통 이거로 기본
                        "max_connections" to "$maxConnection"
                    )
                )
                .build()
        )
        val subnetGroupName = "$logicalName-sg"
        val subnetGroup = SubnetGroup.Builder.create(stack, subnetGroupName)
            .subnetGroupName(subnetGroupName)
            .vpc(vpc)
            .vpcSubnets(
                SubnetSelection.builder()
                    .subnetType(SubnetType.PRIVATE_ISOLATED)
                    .build()
            )
            .description("CDK COMMON RDS SUBNET GROUP FOR $subnetGroupName")
            .build()

        //ServerlessCluster 는 v1에 대한 설정이다
        DatabaseCluster(
            stack, logicalName,
            DatabaseClusterProps.builder()
                .engine(clusterEngine)
                .serverlessV2MinCapacity(minCapacity)
                .serverlessV2MaxCapacity(maxCapacity)
                .parameterGroup(parameterGroup)
                .vpc(vpc)
                .securityGroups(listOf(securityGroup))
                .vpcSubnets(
                    SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_ISOLATED)
                        .build()
                )
//                .writer(ClusterInstance.serverlessV2("${name}-serverlessV2-writer",ServerlessV2ClusterInstanceProps.builder()
//                    .parameterGroup(parameterGroup)
//                    .parameters()
//                    .build()))
                .deletionProtection(true)
                .subnetGroup(subnetGroup)
                .backup(BackupProps.builder().retention(backupRetention.toCdk()).preferredWindow(backupWindow).build())
                .credentials(Credentials.fromGeneratedSecret("admin", CredentialsBaseOptions.builder().secretName(logicalName).build()))
                .cloudwatchLogsRetention(RetentionDays.ONE_WEEK)
                .defaultDatabaseName(logicalName)
                .iamAuthentication(true)
                .storageEncrypted(true)
                .removalPolicy(RemovalPolicy.SNAPSHOT)
                .clusterIdentifier(logicalName) //??
                .build(),
        )
    }

}