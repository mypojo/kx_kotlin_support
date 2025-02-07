package net.kotlinx.aws.lambda.dispatch.asynch

/** 빌드 알림 */
data class EventBridgePipeline(private val event: EventBridgeJson) : EventBridge by event {

    val pipeline = detail["pipeline"].str!!
    val state = detail["state"].str!!

}

