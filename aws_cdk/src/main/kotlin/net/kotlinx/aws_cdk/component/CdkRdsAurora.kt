package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkInterface
import net.kotlinx.aws_cdk.CdkProject
import software.amazon.awscdk.CfnResource
import software.amazon.awscdk.Duration
import software.amazon.awscdk.RemovalPolicy
import software.amazon.awscdk.Stack
import software.amazon.awscdk.customresources.*
import software.amazon.awscdk.services.ec2.*
import software.amazon.awscdk.services.logs.RetentionDays
import software.amazon.awscdk.services.rds.*
import software.amazon.awscdk.services.rds.InstanceProps

/** 재작성 해야함. 샘플임. */
class CdkRdsAurora(
    val project: CdkProject,
    val name: String,
    val vpc: IVpc,
    val securityGroup: ISecurityGroup,
    val maxConnection: Int = 300
) : CdkInterface {

    override val logicalName: String
        get() = "${project.projectName}-rds_${name}"

    fun create(stack: Stack) {

        val clusterEngine = DatabaseClusterEngine.auroraMysql(AuroraMysqlClusterEngineProps.builder().version(AuroraMysqlEngineVersion.VER_3_03_0).build())

        val parameterGroup = ParameterGroup(
            stack, "$logicalName-pg", ParameterGroupProps.builder()
                .engine(clusterEngine)
                .description("CDK - AuroraMysql - $logicalName")
                .parameters(
                    mapOf(
                        "time_zone" to "Asia/Seoul",
                        "transaction_isolation" to "READ-COMMITTED",
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
        val instanceProps = InstanceProps.builder()
            .vpc(vpc)
            .instanceType(InstanceType("serverless"))
            .securityGroups(listOf(securityGroup))
            .vpcSubnets(
                SubnetSelection.builder()
                    .subnetType(SubnetType.PRIVATE_ISOLATED)
//                    .subnets(listOf(vpc.iVpc.privateSubnets[0], vpc.iVpc.isolatedSubnets[0]))
                    .build()
            )
            .build()
        //ServerlessCluster 는 v1에 대한 설정이다
        //DatabaseCluster 를 이용해 우선 생성 후
        //별도 AwsCustomResource를 사용해 설정해줘야 한다.
        //cluster -> custom resource -> serverless v2 instance 생성 하도록 하기 위해
        val databaseCluster = DatabaseCluster(
            stack, logicalName, DatabaseClusterProps.builder()
                .engine(clusterEngine)
                .parameterGroup(parameterGroup)
                .instanceProps(instanceProps)
                .deletionProtection(true)
                .subnetGroup(subnetGroup)
                .backup(BackupProps.builder().retention(Duration.days(7)).preferredWindow("17:00-19:00").build())
                .credentials(Credentials.fromGeneratedSecret("admin", CredentialsBaseOptions.builder().secretName(logicalName).build()))
                .instances(1)
                .cloudwatchLogsRetention(RetentionDays.ONE_WEEK)
                .defaultDatabaseName(logicalName)
                .iamAuthentication(true)
                .storageEncrypted(true)
                .removalPolicy(RemovalPolicy.SNAPSHOT)
                .clusterIdentifier(logicalName) //??
                .build()
        )
        val dbScalingConfigureParam = mapOf(
            "DBClusterIdentifier" to databaseCluster.clusterIdentifier,
        )
        val dbScalingConfigure = AwsCustomResource.Builder.create(stack, "acr_parameter")
            .onCreate(
                AwsSdkCall.builder().service("RDS").action("modifyDBCluster")
                    .parameters(dbScalingConfigureParam)
                    .physicalResourceId(PhysicalResourceId.of(databaseCluster.clusterIdentifier))
                    .build()
            )
            .onUpdate(
                AwsSdkCall.builder().service("RDS").action("modifyDBCluster")
                    .parameters(dbScalingConfigureParam)
                    .physicalResourceId(PhysicalResourceId.of(databaseCluster.clusterIdentifier))
                    .build()
            )
            .policy(AwsCustomResourcePolicy.fromSdkCalls(SdkCallsPolicyOptions.builder().resources(AwsCustomResourcePolicy.ANY_RESOURCE).build()))
            .build()

        val cluster = CfnDBCluster.ServerlessV2ScalingConfigurationProperty.builder().minCapacity(0.5).maxCapacity(16).build()
        val cfnDbCluster = databaseCluster.node.defaultChild as CfnDBCluster
        //serverless v2 의 가용범위를 아래에서 세팅
        //검색시 나오는 AwsCustomResource parameters 에 넣는것은 전혀 안된다..
        cfnDbCluster.setServerlessV2ScalingConfiguration(cluster)
        val dbScalingConfigureTarget = dbScalingConfigure.node.findChild("Resource").node.defaultChild as CfnResource
        cfnDbCluster.addPropertyOverride("EngineMode", "provisioned")
        dbScalingConfigure.node.addDependency(cfnDbCluster)
        (databaseCluster.node.findChild("Instance1") as CfnDBInstance).addDependsOn(dbScalingConfigureTarget)

    }


}