package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkDeploymentType
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.core.DeploymentType
import net.kotlinx.core.DeploymentType.dev
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.batch.CfnComputeEnvironment
import software.amazon.awscdk.services.batch.CfnJobQueue
import software.amazon.awscdk.services.batch.CfnJobQueue.ComputeEnvironmentOrderProperty
import software.amazon.awscdk.services.batch.CfnJobQueueProps

open class CdkBatchJobQueue(
    val project: CdkProject,
    val name: String,
    val priority: Int = 10,
) : CdkDeploymentType {

    override var deploymentType: DeploymentType = dev

    /** VPC 이름 */
    final override val logicalName: String
        get() = "${project.projectName}-queue_${name}-${deploymentType}"

    val arn: String = "arn:aws:batch:ap-northeast-2:${this.project.awsId}:job-queue/${logicalName}"

    lateinit var queue: CfnJobQueue

    fun create(stack: Stack, vararg evns: CfnComputeEnvironment): CdkBatchJobQueue {
        queue = CfnJobQueue(
            stack, logicalName, CfnJobQueueProps.builder()
                .jobQueueName(logicalName)
                .priority(priority)
                .state("ENABLED")
                .computeEnvironmentOrder(
                    evns.mapIndexed { index, it ->
                        ComputeEnvironmentOrderProperty.builder().order(priority * (index + 1)).computeEnvironment(it.computeEnvironmentName).build()
                    }
                )
                .build()
        )
        evns.forEach { queue.addDependency(it) }
        return this
    }

}