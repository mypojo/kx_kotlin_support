package net.kotlinx.aws.lambda.dispatch.asynch


data class EventBridgeEcsTaskStateChange(private val event: EventBridgeJson) : EventBridge by event {

    val stoppedReason = detail["stoppedReason"].str!!
    val group = detail["group"].str!!

}

