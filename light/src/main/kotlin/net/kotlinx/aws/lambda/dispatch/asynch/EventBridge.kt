package net.kotlinx.aws.lambda.dispatch.asynch

import net.kotlinx.aws.lambda.dispatch.AwsLambdaEvent
import net.kotlinx.json.gson.GsonData

/**
 * AwsEventBridge 로 네이밍 하지 않음.. (첨에 잘못했음)
 * */
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