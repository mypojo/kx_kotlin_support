package net.kotlinx.module1.aws.lambda.sqsHandler

import net.kotlinx.core.gson.GsonData

data class LambdaSqsEvent(
    val queueName: String,
    val body: GsonData,
)