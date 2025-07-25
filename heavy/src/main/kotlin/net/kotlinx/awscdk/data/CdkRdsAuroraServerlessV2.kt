package net.kotlinx.awscdk.data

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.toCdk
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

    /** 언더바 포함금지 */
    override val logicalName: String
        get() = "${name}-${suff}"

    lateinit var vpc: IVpc
    lateinit var securityGroup: ISecurityGroup

    /** 이름에 언더바 안됨!! `_` 만 허용 */
    lateinit var name: String

    /** 디폴트 7일 */
    var backupRetention = 7.days

    /** 새벽 2시~4시 */
    var backupWindow = "17:00-19:00"

    /** 1AU = 시간당 0.2$ */
    var capacity = 0.5 to 4

    /** 최대 커넥션 수. 보통 capacity에 따라서 조절  */
    val maxConnection: Int = 200

    /** 개발의 경우 삭제로 해도 됨 */
    var removalPolicy = RemovalPolicy.SNAPSHOT

    /** 결과물 */
    lateinit var databaseCluster: DatabaseCluster

    /**
     * 클러스터 엔진
     * athena 등을 활용할 수 있어서 트랜잭션만 필요한경우 = mysql
     * 트랜잭션 & 리포트 등 select 쿼리 최적화가 필요한경우 = postgresql
     * */
    var clusterEngine = MYSQL

    companion object {
        val MYSQL = DatabaseClusterEngine.auroraMysql(AuroraMysqlClusterEngineProps.builder().version(AuroraMysqlEngineVersion.VER_3_07_1).build())
        val POSTGRESQL = DatabaseClusterEngine.auroraPostgres(AuroraPostgresClusterEngineProps.builder().version(AuroraPostgresEngineVersion.VER_17_4).build())
    }

    fun create(stack: Stack, block: DatabaseClusterProps.Builder.() -> Unit = {}) {

        val parameterGroup = ParameterGroup(
            stack, "pg-$logicalName", ParameterGroupProps.builder()
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

        val subnetGroupName = "sg-$logicalName"
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
        databaseCluster = DatabaseCluster(
            stack, logicalName,
            DatabaseClusterProps.builder()
                .engine(clusterEngine)
                .serverlessV2MinCapacity(capacity.first)
                .serverlessV2MaxCapacity(capacity.second)
                .parameterGroup(parameterGroup)
                .vpc(vpc)
                .securityGroups(listOf(securityGroup))
                .vpcSubnets(
                    SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_ISOLATED)
                        .build()
                )
                .writer(
                    ClusterInstance.serverlessV2(
                        "${name}-serverlessV2Writer-${suff}", ServerlessV2ClusterInstanceProps.builder()
                            .instanceIdentifier("${name}-writer-${suff}") //개별 인스턴스 이름
                            .allowMajorVersionUpgrade(true)
                            .autoMinorVersionUpgrade(true)
                            .enablePerformanceInsights(true) //안하면  Recommendations 에 경고뜸.  2 ACU 이상을 권장함..  비용은 기본설정시(x일) 공짜인듯
                            .build()
                    )
                )
                .deletionProtection(true)
                .subnetGroup(subnetGroup)
                .backup(BackupProps.builder().retention(backupRetention.toCdk()).preferredWindow(backupWindow).build())
                .credentials(Credentials.fromGeneratedSecret("admin", CredentialsBaseOptions.builder().secretName(logicalName).build()))
                .cloudwatchLogsRetention(RetentionDays.ONE_WEEK)
                //.defaultDatabaseName(logicalName)
                .iamAuthentication(true)
                .storageEncrypted(true)
                .removalPolicy(removalPolicy)
                .clusterIdentifier(logicalName) //클러스터 이름
                .enableDataApi(true) //데이터 API 당연히 써야지
                .apply(block)
                .build(),

            //주의 !! Enhanced Monitoring 설정이 아직 없는듯
        )
    }

}