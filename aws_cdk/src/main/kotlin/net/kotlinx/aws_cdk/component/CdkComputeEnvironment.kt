package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkDeploymentType
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.core.DeploymentType
import net.kotlinx.core.DeploymentType.dev
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.batch.CfnComputeEnvironment
import software.amazon.awscdk.services.batch.CfnComputeEnvironment.ComputeResourcesProperty
import software.amazon.awscdk.services.batch.CfnComputeEnvironmentProps
import software.amazon.awscdk.services.ec2.IVpc

enum class ComputeEnvironmentType(val resourceType: String) {
    Normal("FARGATE"),
    Spot("FARGATE_SPOT"),
    ;
}

/** enum 정의 */
class CdkComputeEnvironment(
    val project: CdkProject,
    val type: ComputeEnvironmentType,
) : CdkDeploymentType {

    override var deploymentType: DeploymentType = dev

    /** VPC 이름 */
    override val logicalName: String
        get() = "${project.projectName}_compenv_${type.name}_${deploymentType}"

    lateinit var compEnv: CfnComputeEnvironment

    //ECS 클러스터 이름 수정이 안됨..
    fun create(stack: Stack, iVpc: IVpc, securityGroupIds: List<String>): CdkComputeEnvironment {
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
                .build()
        )
        return this
    }

}