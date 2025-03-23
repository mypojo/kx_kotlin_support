package net.kotlinx.awscdk.ecs

import net.kotlinx.awscdk.CdkInterface
import net.kotlinx.core.Kdsl
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.batch.CfnComputeEnvironment
import software.amazon.awscdk.services.batch.CfnComputeEnvironment.ComputeResourcesProperty
import software.amazon.awscdk.services.batch.CfnComputeEnvironmentProps

class CdkComputeEnvironment : CdkInterface {

    @Kdsl
    constructor(block: CdkComputeEnvironment.() -> Unit = {}) {
        apply(block)
    }

    lateinit var type: ComputeEnvironmentType

    /** 이름 */
    var name: String = "compenv"

    /** VPC 이름 */
    override val logicalName: String
        get() = "${projectName}_${name}_${type.name.lowercase()}-${suff}"

    /** 결과 */
    lateinit var compEnv: CfnComputeEnvironment

    /**
     * VPC는 필수
     * ex) iVpc.privateSubnets.map { it.subnetId }
     *  */
    lateinit var subnets: List<String>

    /** SG */
    lateinit var securityGroupIds: List<String>

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
                        .subnets(subnets)
                        .securityGroupIds(securityGroupIds)
                        .build()
                )
                .apply(block)
                .build()
        )
        return this
    }


}