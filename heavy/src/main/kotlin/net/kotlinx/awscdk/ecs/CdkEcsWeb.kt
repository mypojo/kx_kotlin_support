package net.kotlinx.awscdk.ecs

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.awscdk.basic.CdkLogGroup
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.awscdk.network.PortUtil
import net.kotlinx.awscdk.toCdk
import net.kotlinx.core.Kdsl
import net.kotlinx.lazyLoad.default
import net.kotlinx.system.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps
import software.amazon.awscdk.services.codedeploy.*
import software.amazon.awscdk.services.ec2.ISecurityGroup
import software.amazon.awscdk.services.ec2.IVpc
import software.amazon.awscdk.services.ec2.SubnetSelection
import software.amazon.awscdk.services.ecs.*
import software.amazon.awscdk.services.ecs.Protocol
import software.amazon.awscdk.services.elasticloadbalancingv2.*
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck
import software.amazon.awscdk.services.iam.IRole
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


/**
 * 웹서비스 ECS 구현.
 *
 * 작업전에!
 * 이미지가 ECR에 정상 배포 되었고, 헬스체크가 정상적으로 작동 하는지 확인 해주세요
 * ECS task 들이 전부 ok 되어야 인프라 배포작업이 종료됩니다.
 * */
class CdkEcsWeb : CdkInterface {

    @Kdsl
    constructor(block: CdkEcsWeb.() -> Unit = {}) {
        apply(block)
    }

    override val logicalName: String
        get() = throw UnsupportedOperationException("사용되지않음")

    /**
     * 컨테이너 설정
     * ALB 인증서가 필요합니다!!
     *  */
    lateinit var config: CdkEcsContainerConfig

    /**  웹서비스 이름 */
    var name: String = "web"

    /**
     * 컨테이너가 업무로직을 실행하는데 필요한 역할
     * ex) APP_ADMIN
     *  */
    lateinit var taskRole: IRole

    /**
     * AWS가 컨테이너를 실행할때 필요한 역할
     * ex) ECS_TASK
     *  */
    lateinit var executionRole: IRole

    /**
     * 컨테이너 이미지 with tag
     * 최초 한번만 설정해도 됨
     *  */
    lateinit var image: EcrImage

    /** VPC */
    lateinit var vpc: IVpc

    /** 웹 컨테이너 SG */
    lateinit var sgWeb: ISecurityGroup

    /** ALB SG */
    lateinit var sgAlb: ISecurityGroup

    /** 헬스체크 설정. 많아서 별도분리 */
    var healthCheck: HealthCheck = HealthCheck.builder()
        .interval(30.seconds.toCdk())
        .timeout(10.seconds.toCdk())
        .healthyThresholdCount(3) //디폴트인 5로 하면 체크 전에 내려갈 수 있음.
        .unhealthyThresholdCount(2)
        .path("/common/healthcheck")
        .build()

    /**
     * 스티키 설정. JWT 쓰는경우 설정하지 않아도됨
     * ex) 7.days.toCdk()
     *  */
    var stickinessCookieDuration: Duration? = null

    /** 컨테이너 환경변수 */
    var environment: Map<String, String> = mapOf(
        DeploymentType::class.simpleName!! to deploymentType.name,
    )

    /** 등록할 인증서들 */
    lateinit var certs: List<String>

    /**
     * 라이브 서버일경우 켜기
     * 0.30 per metric-month for the first 10,000 metrics
     * 이걸로 월 5$ 정도 나오는거 같다
     *  */
    var containerInsights: ContainerInsights = ContainerInsights.ENABLED

    /**
     * 온라인으로 만들지.
     * 소규모 작업이면 true
     * WAF 연결할거면 fasle
     * */
    var internetFacing: Boolean = true

    //==================================================== 대부분 고정하는값 ======================================================

    /** 로그 그룹 이름 */
    var logGroupName by default { "ecs/${name}" }


    //==================================================== 결과들 ======================================================

    /** 클러스터 결과물 */
    lateinit var cluster: Cluster

    /** 각종 추가 설정을 달아준다 */
    lateinit var cdkLogGroup: CdkLogGroup

    /** ECS - CLUSTER */
    fun createCluster(stack: Stack) {
        val clusterName = "${projectName}-${name}_cluster-${suff}"
        cluster = Cluster(
            stack, clusterName, ClusterProps.builder()
                .vpc(vpc)
                .clusterName(clusterName)
                .containerInsightsV2(containerInsights)
                .enableFargateCapacityProviders(true) //파게이트 용량공급 설정 가능하게 온
                .build()
        )
        TagUtil.tagDefault(cluster)
    }

    lateinit var targetGroup: IApplicationTargetGroup

    lateinit var taskDef: TaskDefinition

    lateinit var container: ContainerDefinition


    /** ECS - TASK DEFINITION */
    private fun createTaskDefinition(stack: Stack) {
        val taskDefName = "${projectName}-${name}_task_def-${suff}"
        taskDef = TaskDefinition(
            stack, taskDefName, TaskDefinitionProps.builder()
                .family(taskDefName)
                .compatibility(Compatibility.FARGATE)
                .cpu((config.vcpuNum).toString())
                .memoryMiB((1024 * config.memoryGb).toString())
                .runtimePlatform(
                    RuntimePlatform.builder()
                        .cpuArchitecture(CpuArchitecture.X86_64)
                        .operatingSystemFamily(OperatingSystemFamily.LINUX)
                        .build()
                )
                .taskRole(taskRole)
                .executionRole(executionRole)
                .build()
        )
        TagUtil.tagDefault(taskDef)

        val containerName = "${projectName}-${name}_container-${suff}"
        container = taskDef.addContainer(
            containerName, ContainerDefinitionOptions.builder()
                .portMappings(listOf(PortMapping.builder().containerPort(PortUtil.WEB_8080).protocol(Protocol.TCP).build()))
                .containerName(containerName)
                .image(image)
                .environment(environment)
                .logging(
                    run {
                        //일단은 AWS 로그만 지원.  댕댕이 등을 붙일시 추가
                        cdkLogGroup = CdkLogGroup {
                            serviceName = logGroupName
                            create(stack)
                        }
                        LogDriver.awsLogs(
                            AwsLogDriverProps.builder()
                                .logGroup(cdkLogGroup.logGroup)
                                .streamPrefix(name)
                                .build()
                        )
                    }
                )
                .build()
        )
    }

    lateinit var alb: ApplicationLoadBalancer

    private fun createAlb(stack: Stack) {
        val albName = "${projectName}-${name}-alb-${suff}"

        /** 내부 서버의 타임아웃은 이거보다 짧게 설정해야 한다. */
        val duration = (60).seconds.toCdk()  // 2분에서 5분으로 늘림 (전체 리스트 조회시 2분 넘게걸림)

        val albSubnets = if (internetFacing) vpc.publicSubnets else vpc.privateSubnets

        alb = ApplicationLoadBalancer(
            stack, albName, ApplicationLoadBalancerProps.builder()
                .vpc(vpc)
                .internetFacing(internetFacing)
                .loadBalancerName(albName)
                .idleTimeout(duration)
                .internetFacing(true)
                .vpcSubnets(SubnetSelection.builder().subnets(albSubnets).build()) //ALB는 public에 있어야함
                .securityGroup(sgAlb)
                .build()
        )

        /** 기본 리다이렉트 설정 */
        alb.addListener(
            "${projectName}-${name}_alb_listner_http-${suff}", BaseApplicationListenerProps.builder()
                .port(PortUtil.WEB_80) //80은 디폴트라서 빼도 되긴 함
                .protocol(ApplicationProtocol.HTTP)
                .defaultAction(
                    ListenerAction.redirect(
                        RedirectOptions.builder()
                            .port(PortUtil.WEB_443.toString())
                            .protocol(ApplicationProtocol.HTTPS.name) //enum 이 아니고 String임.. 개판이다.
                            .permanent(true)  //302 대신 301(영구)로 리턴해야함
                            .build()
                    )
                )
                .build()
        )
        TagUtil.tagDefault(alb)
    }

    /**
     * 롤링 서비스 생성
     * 보통 개발서버로 사용됨
     *  */
    fun createServiceRolling(stack: Stack) {
        createCluster(stack)
        createTaskDefinition(stack)
        createAlb(stack)

        val service = createFargateService(stack)

        val targetGroupName = "${projectName}-${name}-target-${suff}" //언더바 사용 금지
        this.targetGroup = ApplicationTargetGroup(
            stack, targetGroupName, ApplicationTargetGroupProps.builder()
                .vpc(vpc)
                .targetType(TargetType.IP)
                .targetGroupName(targetGroupName)
                .protocol(ApplicationProtocol.HTTP)
                .port(PortUtil.WEB_8080)
                .healthCheck(healthCheck)
                .apply {
                    stickinessCookieDuration?.let { stickinessCookieDuration(it.toCdk()) }
                }
                .build()
        )
        TagUtil.tagDefault(targetGroup)

        alb.addListener(
            "${projectName}-${name}_alb_listner_https-${suff}",
            BaseApplicationListenerProps.builder()
                .port(PortUtil.WEB_443)
                .protocol(ApplicationProtocol.HTTPS) //443 하면 디폴트라서 빼도됨
                .open(false) //오픈 옵션 주면, 자동으로 SG에 전체 오픈이 추가됨. 실제운영시에는 별도 SG를 사용함
                .sslPolicy(SslPolicy.RECOMMENDED) //최신버전 쓰자
                .certificates(certs.map { ListenerCertificate.fromArn(it) })
                .defaultAction(ListenerAction.forward(listOf(targetGroup))) // 타겟그룹 연결
                .build()
        )
        service.attachToApplicationTargetGroup(targetGroup)

        // 기존 EcsTarget 방식을 ,직접 타겟그룹을 생성하는 방식으로 변경 (확장때문)
//        val ecsTarget = EcsTarget.builder()
//            .containerName(container.containerName)
//            .containerPort(PortUtil.WEB_8080)
//            .newTargetGroupId(targetGroupName)
//            .listener(
//                ListenerConfig.applicationListener(
//                    httpsListner, AddApplicationTargetsProps.builder()
//                        .protocol(ApplicationProtocol.HTTP)
//                        .port(PortUtil.WEB_8080)
//                        .healthCheck(healthCheck)
//                        .targetGroupName(targetGroupName)
//                        .apply {
//                            stickinessCookieDuration?.let { stickinessCookieDuration(it.toCdk()) }
//                        }
//                        .build()
//                )
//            )
//            .build()
//        service.registerLoadBalancerTargets(ecsTarget)

    }

    private fun createFargateService(stack: Stack, block: FargateServiceProps.Builder.() -> Unit = {}): FargateService {
        val serviceName = "${projectName}-${name}_service-${suff}"
        val service = FargateService(
            stack, serviceName, FargateServiceProps.builder()
                .cluster(cluster)
                .serviceName(serviceName)
                .assignPublicIp(false) // public ip 사용할일 없음
                .taskDefinition(taskDef)
                .desiredCount(config.desiredCount)
                .healthCheckGracePeriod(3.minutes.toCdk()) // CI/CD 참고
                .vpcSubnets(SubnetSelection.builder().subnets(vpc.privateSubnets).build()) //컨테이너는 public에 노출 안함
                .securityGroups(listOf(sgWeb))
                .minHealthyPercent(100)
                .maxHealthyPercent(200) //빌드 등의 기능 수행시 최대 desiredCount 의 200% 사용가능
                .enableEcsManagedTags(true)
                .apply(block)
                .build()
        )
        TagUtil.tagDefault(service)
        val autoScale = service.autoScaleTaskCount(
            EnableScalingProps.builder()
                .maxCapacity(config.maxCapacity)
                .minCapacity(config.minCapacity)
                .build()

        )
        autoScale.scaleOnCpuUtilization(
            "${projectName}-${name}_autoScale-${suff}", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(config.targetUtilizationPercent)
                .scaleInCooldown(300.seconds.toCdk()) //디폴트 그대로
                .scaleOutCooldown(300.seconds.toCdk()) //디폴트 그대로
                .build()
        )
        return service
    }

    /**
     * 보통 라이브 서버로 이용
     * */
    fun createServiceBlueGreen(stack: Stack) {
        createCluster(stack)
        createTaskDefinition(stack)
        createAlb(stack)

        /** 타겟그룹 2개 있어야함 -> 생셩기 별도로 분리 */
        fun makeTargetGroup(targetGroupType: String): ApplicationTargetGroup {
            val targetGroupName = "${projectName}-${name}-${targetGroupType}-${suff}"
            val targetGroup = ApplicationTargetGroup(
                stack, targetGroupName, ApplicationTargetGroupProps.builder()
                    .vpc(vpc)
                    .targetType(TargetType.IP)
                    .targetGroupName(targetGroupName)
                    .port(PortUtil.WEB_8080) //서비스 포트와 동일하게
                    .healthCheck(healthCheck)
                    .build()
            )
            TagUtil.tagDefault(targetGroup)
            return targetGroup
        }


        val blue = "blue".let { targetGroupType ->
            val targetGroup = makeTargetGroup(targetGroupType)
            val listener = alb.addListener(
                "${projectName}-${name}_albl-${targetGroupType}-${suff}",
                BaseApplicationListenerProps.builder()
                    .port(PortUtil.WEB_443)
                    .protocol(ApplicationProtocol.HTTPS) //443 하면 디폴트라서 빼도됨
                    .defaultAction(ListenerAction.forward(listOf(targetGroup)))
                    .open(false) //오픈 옵션 주면, 자동으로 SG에 전체 오픈이 추가됨. 실제운영시에는 별도 SG를 사용함
                    .sslPolicy(SslPolicy.RECOMMENDED) //최신버전 쓰자
                    .certificates(certs.map { ListenerCertificate.fromArn(it) })
                    .build()
            )
            targetGroup to listener
        }

        val green = "green".let { targetGroupType ->
            val targetGroup = makeTargetGroup(targetGroupType)
            val listener = alb.addListener(
                "${projectName}-${name}_albl-${targetGroupType}-${suff}",
                BaseApplicationListenerProps.builder()
                    .port(PortUtil.WEB_8080)
                    .protocol(ApplicationProtocol.HTTP)
                    .defaultAction(ListenerAction.forward(listOf(targetGroup)))
                    .open(false) //오픈 옵션 주면, 자동으로 SG에 전체 오픈이 추가됨. 실제운영시에는 별도 SG를 사용함
                    .build()
            )
            targetGroup to listener
        }

        //==================================================== ECS - SERVICE ======================================================
        val service = createFargateService(stack) {
            //블루그린은 코드디플로이 적용
            deploymentController(DeploymentController.builder().type(DeploymentControllerType.CODE_DEPLOY).build())
        }
        service.attachToApplicationTargetGroup(blue.first) //최초 타겟그룹을 지정해준다.

        //서비스 생성 -> 코드디플로이 생성.
        val codedeployApplicationName = "${projectName}-${name}_codedeploy-${suff}"
        val codedeployApplication = EcsApplication(stack, codedeployApplicationName, EcsApplicationProps.builder().applicationName(codedeployApplicationName).build())
        val codedeployApplicationGroupName = "${projectName}-${name}_codedeploy_group-${suff}"
        EcsDeploymentGroup.Builder.create(stack, codedeployApplicationGroupName)
            .deploymentGroupName(codedeployApplicationGroupName)
            .application(codedeployApplication)
            .service(service)
            .role(taskRole)  //일단 다준다..
            .blueGreenDeploymentConfig(
                EcsBlueGreenDeploymentConfig.builder()
                    .blueTargetGroup(blue.first)
                    .greenTargetGroup(green.first)
                    .listener(blue.second)
                    .testListener(green.second)
                    .terminationWaitTime(15.minutes.toCdk())
                    .build()
            )
            .deploymentConfig(EcsDeploymentConfig.ALL_AT_ONCE) //한방에 전부
            .build()
    }

    /**
     * 특수한 포트를 설정해서 오픈함
     * 소스코드 참고용
     * ex) XX 포트의 경우 all open 으로 열어두지만 특수 경로만 하용됨 -> ListenerCondition.pathPatterns(listOf("/api/xx/basic/call/..*")
     *  */
    fun addListenerCustomPort(port: Int, conditions: List<ListenerCondition>) {
        /** 8081 특수포트 설정 */
        val listener = alb.addListener(
            "${projectName}-${name}_alb_listner_$port-${suff}", BaseApplicationListenerProps.builder()
                .port(port)
                .protocol(ApplicationProtocol.HTTP)
                // 기본 동작을 403으로 고정 응답. 조건과 일치하지 않는 모든 요청은 403을 반환하게 함
                .defaultAction(
                    ListenerAction.fixedResponse(
                        403, FixedResponseOptions.builder()
                            .contentType("text/plain")
                            .messageBody("Forbidden")
                            .build()
                    )
                )
                .build()
        )
        listener.addAction(
            "${projectName}-${name}_alb_action_$port-${suff}",
            AddApplicationActionProps.builder()
                .priority(10)
                .conditions(conditions)
                .action(ListenerAction.forward(listOf(targetGroup)))
                .build()
        )
    }
}