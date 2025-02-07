package net.kotlinx.aws.lambda.dispatch.asynch

import net.kotlinx.aws.lambda.dispatch.AwsLambdaEvent
import net.kotlinx.json.gson.GsonData

interface EventBridge : AwsLambdaEvent {
    val body: GsonData
    val detailType: String
    val account: String
    val region: String
    val time: String
    val source: String
    val resources: List<String>
    val detail: GsonData
}