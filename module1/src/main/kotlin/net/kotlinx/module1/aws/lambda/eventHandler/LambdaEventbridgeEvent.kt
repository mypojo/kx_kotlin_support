package net.kotlinx.module1.aws.lambda.eventHandler

data class LambdaEventbridgeEvent(
    /** 이거 하나뿐이다. */
    val eventName: String,
)