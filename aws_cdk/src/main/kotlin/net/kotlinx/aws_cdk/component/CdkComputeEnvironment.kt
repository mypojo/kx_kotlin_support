package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkDeploymentType
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.core.DeploymentType
import net.kotlinx.core.DeploymentType.DEV
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.batch.CfnComputeEnvironment
import software.amazon.awscdk.services.batch.CfnComputeEnvironment.ComputeResourcesProperty
import software.amazon.awscdk.services.batch.CfnComputeEnvironmentProps
import software.amazon.awscdk.services.ec2.IVpc

/** 접두어 소문자 주의! (네이밍 때문)  */
enum class ComputeEnvironmentType(val resourceType: String) {
    normal("FARGATE"),
    spot("FARGATE_SPOT"),
    ;
}

/** enum 정의 */
class CdkComputeEnvironment(
    val project: CdkProject,
    val type: ComputeEnvironmentType,
) : CdkDeploymentType {

    override var deploymentType: DeploymentType = DEV

    /** VPC 이름 */
    override val logicalName: String
        get() = "${project.projectName}_compenv_${type.name}-${deploymentType.name.lowercase()}"

    lateinit var iVpc: IVpc

    lateinit var securityGroupIds: List<String>

    /** 결과 */
    lateinit var compEnv: CfnComputeEnvironment

    //ECS 클러스터 이름 수정이 안됨..
    fun create(stack: Stack,block: CfnComputeEnvironmentProps.Builder.() -> Unit = {}): CdkComputeEnvironment {
        compEnv = CfnComputeEnvironment(
            stack, logicalName, CfnComputeEnvironmentProps.builder()
                .computeEnvironmentName(logicalName)
                .type("MANAGED")  //관리형
                .state("ENABLED")
                .computeResources(
                    ComputeResourcesProperty.builder()
                        .maxvCpus(100)
                        .type(type.resourceType)
                        .subnets(iVpc.privateSubnets.map { it.subnetId })
                        .securityGroupIds(securityGroupIds)
                        .build()
                )
                .apply(block)
                .build()
        )
        return this
    }

}