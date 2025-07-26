package net.kotlinx.aws.lambda.dispatch.asynch

import net.kotlinx.aws.lambda.dispatch.AwsLambdaEvent
import net.kotlinx.json.gson.GsonData


/** 간단 노티 */
data class SnsNotification(val subject: String, val message: String) : AwsLambdaEvent

/**
 * 알람 걸면 오는거
 * val msg = "${data["Namespace"]} ${data["MetricName"]}\n${sns["NewStateReason"]}"
 * */
data class SnsAlarm(val alarmName: String, val data: GsonData) : AwsLambdaEvent

/** 등록 안된거  */
data class SnsUnknown(val data: GsonData) : AwsLambdaEvent