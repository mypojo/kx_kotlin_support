package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkInterface
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.DeploymentType
import net.kotlinx.aws_cdk.DeploymentType.dev
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.batch.CfnComputeEnvironment
import software.amazon.awscdk.services.batch.CfnJobQueue
import software.amazon.awscdk.services.batch.CfnJobQueue.ComputeEnvironmentOrderProperty
import software.amazon.awscdk.services.batch.CfnJobQueueProps

open class CdkBatchJobQueue(
    val project: CdkProject,
    val name: String,
    val priority: Int = 10,
) : CdkInterface {

    var deploymentType: DeploymentType = dev

    /** VPC 이름 */
    open override val logicalName: String
        get() = "${project.projectName}-queue_${name}-${deploymentType}"

    val arn: String = "arn:aws:batch:ap-northeast-2:${this.project.awsId}:job-queue/${name}"

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