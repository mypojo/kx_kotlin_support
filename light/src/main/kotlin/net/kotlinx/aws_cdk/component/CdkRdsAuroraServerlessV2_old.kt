//package net.kotlinx.aws_cdk.component
//
//import net.kotlinx.aws_cdk.CdkInterface
//import net.kotlinx.aws_cdk.toCdk
//import software.amazon.awscdk.CfnResource
//import software.amazon.awscdk.RemovalPolicy
//import software.amazon.awscdk.Stack
//import software.amazon.awscdk.customresources.*
//import software.amazon.awscdk.services.ec2.ISecurityGroup
//import software.amazon.awscdk.services.ec2.IVpc
//import software.amazon.awscdk.services.ec2.SubnetSelection
//import software.amazon.awscdk.services.ec2.SubnetType
//import software.amazon.awscdk.services.logs.RetentionDays
//import software.amazon.awscdk.services.rds.*
//import kotlin.time.Duration.Companion.days
//
///** 재작성 해야함. 샘플임. */
//class CdkRdsAuroraServerlessV2_old : CdkInterface {
//
//    override val logicalName: String
//        get() = "${project.projectName}-rds_${name}"
//
//    lateinit var vpc: IVpc
//    lateinit var securityGroup: ISecurityGroup
//
//    lateinit var name: String
//
//    val maxConnection: Int = 300
//
//    /** 인스턴스 수.  */
//    var instanceCnt = 1
//
//    /** 디폴트 7일 */
//    var backupRetention = 7.days
//
//    /** 새벽 2시~4시 */
//    var backupWindow = "17:00-19:00"
//
//    /** 최소 AU */
//    var minCapacity = 0.5
//
//    /** 최대 AU */
//    var maxCapacity = 8
//
//    fun create(stack: Stack) {
//
//        val clusterEngine = DatabaseClusterEngine.auroraMysql(AuroraMysqlClusterEngineProps.builder().version(AuroraMysqlEngineVersion.VER_3_03_0).build())
//
//        val parameterGroup = ParameterGroup(
//            stack, "$logicalName-pg", ParameterGroupProps.builder()
//                .engine(clusterEngine)
//                .description("CDK - AuroraMysql - $logicalName")
//                .parameters(
//                    mapOf(
//                        "time_zone" to "Asia/Seoul",
//                        "transaction_isolation" to "READ-COMMITTED", //보통 이거로 기본
//                        "max_connections" to "$maxConnection"
//                    )
//                )
//                .build()
//        )
//        val subnetGroupName = "$logicalName-sg"
//        val subnetGroup = SubnetGroup.Builder.create(stack, subnetGroupName)
//            .subnetGroupName(subnetGroupName)
//            .vpc(vpc)
//            .vpcSubnets(
//                SubnetSelection.builder()
//                    .subnetType(SubnetType.PRIVATE_ISOLATED)
//                    .build()
//            )
//            .description("CDK COMMON RDS SUBNET GROUP FOR $subnetGroupName")
//            .build()
//
//        val instanceProps = InstanceProps.builder()
//            .vpc(vpc)
//            //.instanceType(InstanceType("serverless"))  // XXXXX 이거 확인해야함
//            .securityGroups(listOf(securityGroup))
//            .vpcSubnets(
//                SubnetSelection.builder()
//                    .subnetType(SubnetType.PRIVATE_ISOLATED)
//                    .build()
//            )
//            .build()
//
//        //ServerlessCluster 는 v1에 대한 설정이다
//        //DatabaseCluster 를 이용해 우선 생성 후
//        //별도 AwsCustomResource를 사용해 설정해줘야 한다.
//        //cluster -> custom resource -> serverless v2 instance 생성 하도록 하기 위해
//        val cluster = DatabaseCluster(
//            stack, logicalName, DatabaseClusterProps.builder()
//                .engine(clusterEngine)
//                .serv
//                .parameterGroup(parameterGroup)
//
////                .instanceProps(instanceProps)
////                .instances(instanceCnt)
//                .writer(ClusterInstance.serverlessV2("${name}-serverlessV2-writer",ServerlessV2ClusterInstanceProps.builder()
//                    .parameterGroup(parameterGroup)
//                    .parameters()
//                    .build()))
//
//                .deletionProtection(true)
//                .subnetGroup(subnetGroup)
//                .backup(BackupProps.builder().retention(backupRetention.toCdk()).preferredWindow(backupWindow).build())
//                .credentials(Credentials.fromGeneratedSecret("admin", CredentialsBaseOptions.builder().secretName(logicalName).build()))
//                .cloudwatchLogsRetention(RetentionDays.ONE_WEEK)
//                .defaultDatabaseName(logicalName)
//                .iamAuthentication(true)
//                .storageEncrypted(true)
//                .removalPolicy(RemovalPolicy.SNAPSHOT)
//                .clusterIdentifier(logicalName) //??
//                .build()
//        )
//
//        val dbScalingConfigureParam = mapOf(
//            "DBClusterIdentifier" to cluster.clusterIdentifier,
//        )
//        val dbScalingConfigure = AwsCustomResource.Builder.create(stack, "acr_parameter")
//            .onCreate(
//                AwsSdkCall.builder().service("RDS").action("modifyDBCluster")
//                    .parameters(dbScalingConfigureParam)
//                    .physicalResourceId(PhysicalResourceId.of(cluster.clusterIdentifier))
//                    .build()
//            )
//            .onUpdate(
//                AwsSdkCall.builder().service("RDS").action("modifyDBCluster")
//                    .parameters(dbScalingConfigureParam)
//                    .physicalResourceId(PhysicalResourceId.of(cluster.clusterIdentifier))
//                    .build()
//            )
//            .policy(AwsCustomResourcePolicy.fromSdkCalls(SdkCallsPolicyOptions.builder().resources(AwsCustomResourcePolicy.ANY_RESOURCE).build()))
//            .build()
//
//        (cluster.node.defaultChild as CfnDBCluster).also { cfnDbCluster ->
//            //serverless v2 의 가용범위를 아래에서 세팅
//            //검색시 나오는 AwsCustomResource parameters 에 넣는것은 전혀 안된다..
//            val clusterScaling = CfnDBCluster.ServerlessV2ScalingConfigurationProperty.builder()
//                .minCapacity(minCapacity)
//                .maxCapacity(maxCapacity)
//                .build()
//            cfnDbCluster.setServerlessV2ScalingConfiguration(clusterScaling)
//            cfnDbCluster.addPropertyOverride("EngineMode", "provisioned")
//            dbScalingConfigure.node.addDependency(cfnDbCluster)
//        }
//
//        (cluster.node.findChild("Instance1") as CfnDBInstance).also {
//            val dbScalingConfigureTarget = dbScalingConfigure.node.findChild("Resource").node.defaultChild as CfnResource
//            it.addDependsOn(dbScalingConfigureTarget)
//        }
//
//    }
//
//
//}