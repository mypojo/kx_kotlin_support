package net.kotlinx.aws.lambda.dispatch.asynch


data class EventBridgeAwsBatchStateChange(private val event: EventBridgeJson) : EventBridge by event {

    val jobName = detail["jobName"].str!!
    val jobId = detail["jobId"].str!!
    val jobQueue = detail["jobQueue"].str!!
    val status = detail["status"].str!!
    val statusReason = detail["statusReason"].str!!
    val jobDefinition = detail["jobDefinition"].str!!

}

