package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkInterface
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.DeploymentType
import net.kotlinx.aws_cdk.DeploymentType.dev
import net.kotlinx.aws_cdk.util.TagUtil
import net.kotlinx.core1.number.tuHour
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.batch.CfnJobDefinition
import software.amazon.awscdk.services.batch.CfnJobDefinition.*
import software.amazon.awscdk.services.batch.CfnJobDefinitionProps
import software.amazon.awscdk.services.ecs.EcrImage
import software.amazon.awscdk.services.iam.IRole

open class CdkBatchJobDefinition(
    val project: CdkProject,
    val name: String,
    val vcpu: String,
    val memory: Long,
) : CdkInterface {

    var deploymentType: DeploymentType = dev

    /** VPC 이름 */
    open override val logicalName: String
        get() = "${project.projectName}-${name}-${deploymentType}"

    val arn: String = "arn:aws:batch:ap-northeast-2:${this.project.awsId}:job-definition/${logicalName}"

    lateinit var jobDef: CfnJobDefinition

    fun create(stack: Stack, props: CfnJobDefinitionProps = props()): CdkBatchJobDefinition {
        jobDef = CfnJobDefinition(stack, "batchJobDefinition-${logicalName}", props)
        TagUtil.tag(jobDef, deploymentType)
        return this
    }

    lateinit var ecrImage: EcrImage
    lateinit var jobRole: IRole
    lateinit var executionRole: IRole
    lateinit var logGroupPath: String

    /**
     * 긴단한 VPC 생성 props
     * 이름 중복으로 작업 진행이 안되는경우
     * 1. 주석 처리해서 돌림 ->   Job definitions off 됨
     * 2. 주석 풀고 다시 돌림 -> Job definitions 버전 올라가면서 활성화됨
     *  */
    open fun props(): CfnJobDefinitionProps = CfnJobDefinitionProps.builder()
        .jobDefinitionName(logicalName)
        .platformCapabilities(listOf("FARGATE")) //파게이트만
        .type("Container")
        //.retryStrategy(RetryStrategyProperty.builder().attempts(1).build()) //자체 재시도 하지 않음. 최소 1~10 까지 지정
        .timeout(TimeoutProperty.builder().attemptDurationSeconds(12L.tuHour().sec).build()) //12시간
        .containerProperties(
            ContainerPropertiesProperty.builder()
                .fargatePlatformConfiguration(FargatePlatformConfigurationProperty.builder().platformVersion("1.4.0").build())
                .command(listOf("Ref::JOB_CONFIG")) //job submit 할때의 파라메터와 일치해아함 (BatchUtil 참고)
                .environment(
                    listOf(
                        "DeploymentType" to deploymentType.name
                    )
                )
                .image(ecrImage.imageName)
                .resourceRequirements(
                    listOf(
                        ResourceRequirementProperty.builder().type("VCPU").value(vcpu).build(),
                        ResourceRequirementProperty.builder().type("MEMORY").value("${1024 * memory}").build(),
                    )
                )
                .jobRoleArn(jobRole.roleArn)
                .executionRoleArn(jobRole.roleArn)
                .logConfiguration(
                    LogConfigurationProperty.builder()
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
        .build()
}