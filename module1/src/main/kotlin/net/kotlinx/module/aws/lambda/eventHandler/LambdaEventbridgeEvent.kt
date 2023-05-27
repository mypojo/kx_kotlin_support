package net.kotlinx.module.aws.lambda.eventHandler

data class LambdaEventbridgeEvent(
    /** 이거 하나뿐이다. */
    val eventName: String,
)