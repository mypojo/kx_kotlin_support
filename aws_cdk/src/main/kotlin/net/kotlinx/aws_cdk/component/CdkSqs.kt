package net.kotlinx.aws_cdk.component

import net.kotlinx.aws_cdk.CdkDeploymentType
import net.kotlinx.aws_cdk.CdkProject
import net.kotlinx.aws_cdk.toCdk
import net.kotlinx.core.DeploymentType
import software.amazon.awscdk.Stack
import software.amazon.awscdk.services.sqs.IQueue
import software.amazon.awscdk.services.sqs.Queue
import software.amazon.awscdk.services.sqs.QueueProps

/** enum */
class CdkSqs(
    val project: CdkProject,
    val name: String,
) : CdkDeploymentType {

    override var deploymentType: DeploymentType = DeploymentType.dev

    override val logicalName: String
        get() = "${project.projectName}-${name}-${deploymentType}"

    /**  */
    var visibilityTimeout: kotlin.time.Duration? = null

    var fifo: Boolean = false

    lateinit var iQueue: IQueue

    fun create(stack: Stack): CdkSqs {
        val props = QueueProps.builder()
            .visibilityTimeout(visibilityTimeout?.toCdk())  //자주 확인 가능하도록 짧게 줌. 디폴트 30초임
            .fifo(fifo)
            .queueName(this.logicalName)
            .build()
        iQueue = Queue(stack, "sqs-$logicalName", props)
        return this
    }

    fun load(stack: Stack): CdkSqs {
        iQueue = Queue.fromQueueArn(stack, "sqs-$logicalName", "arn:aws:sqs:ap-northeast-2:${project.awsId}:${logicalName}")
        return this
    }

}