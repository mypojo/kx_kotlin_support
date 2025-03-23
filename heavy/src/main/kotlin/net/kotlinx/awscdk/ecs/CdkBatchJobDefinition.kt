package net.kotlinx.awscdk.ecs

import net.kotlinx.aws.batch.BatchUtil
import net.kotlinx.awscdk.CdkEnum
import net.kotlinx.awscdk.basic.TagUtil
import net.kotlinx.system.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.batch.CfnJobDefinition
import software.amazon.awscdk.services.batch.CfnJobDefinition.RuntimePlatformProperty
import software.amazon.awscdk.services.batch.CfnJobDefinitionProps
import software.amazon.awscdk.services.ecs.EcrImage
import software.amazon.awscdk.services.iam.IRole
import kotlin.time.Duration.Companion.hours

/**
 * enum 정의
 *
 * 1cpu 최대 8G
 * 2cpu 최대 16G
 * 4cpu 최대 30G ..
 * 16cpu 최대 120G
 *  */
class CdkBatchJobDefinition(
    val name: String,
    val vcpu: String,
    val memory: Long,
) : CdkEnum {

    /** VPC 이름 */
    override val logicalName: String
        get() = "${projectName}-${name}-${suff}"

    val arn: String
        get() = "arn:aws:batch:ap-northeast-2:${awsConfig.awsId}:job-definition/${logicalName}"

    lateinit var jobDef: CfnJobDefinition
    lateinit var ecrImage: EcrImage
    lateinit var jobRole: IRole
    lateinit var executionRole: IRole
    lateinit var logGroupPath: String

    /**
     * publicIp가 필요한지?
     * 퍼블릭망에서는 보통 공개 IP를 할당받음 -> 이래야 ECR 등의 인터넷망에 접근됨
     *  */
    var publicIp: Boolean = false

    /**
     * args 로 사용할 인자값 매핑
     * job submit 할때의 파라메터와 일치해아함
     * */
    var command: List<String> = listOf(
        "Ref::${BatchUtil.BATCH_ARGS01}",
    )

    /** 디폴트로 12시간. 프로젝트에 따라 조절할거시 */
    var attemptDurationSeconds: Long = 12.hours.inWholeSeconds

    /**
     * X86_64 or ARM64
     * ARM64 로 빌드시 java 실행이 안될 수 있음!
     *  */
    var cpuArchitecture: String = "X86_64"

    /**
     * 환경변수. 여기에 더 추가할것
     * ex) += Spring.ENV_PROFILE to "default,dev"
     *  */
    var environment: Map<String, String> = mapOf(
        DeploymentType::class.simpleName!! to deploymentType.name
    )

    /**
     * 이름 중복으로 작업 진행이 안되는경우
     * 1. 주석 처리해서 돌림 ->   Job definitions off 됨
     * 2. 주석 풀고 다시 돌림 -> Job definitions 버전 올라가면서 활성화됨
     *  */
    fun create(stack: Stack, block: CfnJobDefinitionProps.Builder.() -> Unit = {}): CdkBatchJobDefinition {

        val props = CfnJobDefinitionProps.builder()
            .jobDefinitionName(logicalName)
            .platformCapabilities(listOf("FARGATE")) //파게이트만
            .type("Container")
            //.retryStrategy(RetryStrategyProperty.builder().attempts(1).build()) //자체 재시도 하지 않음. 최소 1~10 까지 지정
            .timeout(CfnJobDefinition.TimeoutProperty.builder().attemptDurationSeconds(attemptDurationSeconds).build())

            .containerProperties(
                CfnJobDefinition.ContainerPropertiesProperty.builder()
                    .fargatePlatformConfiguration(CfnJobDefinition.FargatePlatformConfigurationProperty.builder().platformVersion("1.4.0").build())
                    .runtimePlatform(
                        RuntimePlatformProperty.builder()
                            .operatingSystemFamily("LINUX")
                            .cpuArchitecture(cpuArchitecture)
                            .build()
                    )
                    .command(command)
                    .environment(environment.entries.map { e -> CfnJobDefinition.EnvironmentProperty.builder().name(e.key).value(e.value).build() })
                    .image(ecrImage.imageName)
                    .resourceRequirements(
                        listOf(
                            CfnJobDefinition.ResourceRequirementProperty.builder().type("VCPU").value(vcpu).build(),
                            CfnJobDefinition.ResourceRequirementProperty.builder().type("MEMORY").value("${1024 * memory}").build(),
                        )
                    )
                    .jobRoleArn(jobRole.roleArn)
                    .executionRoleArn(jobRole.roleArn)
                    .logConfiguration(
                        CfnJobDefinition.LogConfigurationProperty.builder()
                            .logDriver("awslogs")
                            .options(
                                mapOf(
                                    "awslogs-group" to logGroupPath,
                                    "awslogs-create-group" to "true",
                                    "awslogs-stream-prefix" to logicalName,
                                )
                            )
                            .build()
                    )
                    .apply {
                        if (publicIp) {
                            networkConfiguration(CfnJobDefinition.NetworkConfigurationProperty.builder().assignPublicIp("ENABLED").build())
                        }
                    }
                    .build()
            )
            .apply(block)
            .build()

        jobDef = CfnJobDefinition(stack, "batchJobDefinition-${logicalName}", props)
        TagUtil.tagDefault(jobDef)
        return this
    }


}