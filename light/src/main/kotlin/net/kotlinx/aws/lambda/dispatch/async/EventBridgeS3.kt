package net.kotlinx.aws.lambda.dispatch.async

data class EventBridgeS3(private val event: EventBridgeJson) : EventBridge by event {
    val bucket: String = detail["bucket"]["name"].str!!
    val key: String = detail["object"]["key"].str!!
    val reason: String = detail["reason"].str!!
}