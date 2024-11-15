package net.kotlinx.awscdk.ecs

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.batch.CfnComputeEnvironment
import software.amazon.awscdk.services.batch.CfnComputeEnvironment.ComputeResourcesProperty
import software.amazon.awscdk.services.batch.CfnComputeEnvironmentProps
import software.amazon.awscdk.services.ec2.IVpc

enum class ComputeEnvironmentType(val resourceType: String) {
    NORMAL("FARGATE"),
    SPOT("FARGATE_SPOT"),
    ;
}

class CdkComputeEnvironment : CdkInterface {

    @Kdsl
    constructor(block: CdkComputeEnvironment.() -> Unit = {}) {
        apply(block)
    }

    lateinit var type: ComputeEnvironmentType

    /** VPC 이름 */
    override val logicalName: String
        get() = "${projectName}_compenv_${type.name.lowercase()}-${suff}"

    lateinit var iVpc: IVpc

    lateinit var securityGroupIds: List<String>

    /** 결과 */
    lateinit var compEnv: CfnComputeEnvironment

    //ECS 클러스터 이름 수정이 안됨..
    fun create(stack: Stack, block: CfnComputeEnvironmentProps.Builder.() -> Unit = {}): CdkComputeEnvironment {
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