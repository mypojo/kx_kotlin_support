package net.kotlinx.aws.lambda.dispatch.asynch

import net.kotlinx.aws.lambda.dispatch.AwsLambdaEvent
import net.kotlinx.json.gson.GsonData

//향후 3개 객체를 object로 합치자..


/** 간단 노티 */
data class AwsSnsNotification(val body: GsonData) : AwsLambdaEvent {
    val subject = body["Subject"].str!!
    val message = body["Message"].str!!
}

/**
 * 알람 걸면 오는거
 * val msg = "${data["Namespace"]} ${data["MetricName"]}\n${sns["NewStateReason"]}"
 * */
data class AwsSnsAlarm(val body: GsonData) : AwsLambdaEvent {
    val alarmName = body["AlarmName"].str!!
    val data = body["Trigger"]
}

/** 등록 안된거  */
data class AwsSnsUnknown(val body: GsonData) : AwsLambdaEvent