package net.kotlinx.aws_cdk.component

import net.kotlinx.aws.batch.BatchUtil
import net.kotlinx.aws_cdk.CdkEnum
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.batch.CfnJobDefinition
import software.amazon.awscdk.services.batch.CfnJobDefinitionProps
import software.amazon.awscdk.services.ecs.EcrImage
import software.amazon.awscdk.services.iam.IRole
import kotlin.time.Duration.Companion.hours

/** enum 정의 */
class CdkBatchJobDefinition(
    val name: String,
    val vcpu: String,
    val memory: Long,
) : CdkEnum {

    /** VPC 이름 */
    override val logicalName: String
        get() = "${project.projectName}-${name}-${deploymentType.name.lowercase()}"

    val arn: String
        get() = "arn:aws:batch:ap-northeast-2:${project.awsId}:job-definition/${logicalName}"

    lateinit var jobDef: CfnJobDefinition
    lateinit var ecrImage: EcrImage
    lateinit var jobRole: IRole
    lateinit var executionRole: IRole
    lateinit var logGroupPath: String

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
                    .command(command)
                    .environment(
                        listOf(
                            CfnJobDefinition.EnvironmentProperty.builder().name(DeploymentType::class.simpleName).value(deploymentType.name).build(),
                        )
                    )
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
                    .build()
            )
            .apply(block)
            .build()

        jobDef = CfnJobDefinition(stack, "batchJobDefinition-${logicalName}", props)
        TagUtil.tag(jobDef, deploymentType)
        return this
    }


}