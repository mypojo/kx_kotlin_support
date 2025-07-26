package net.kotlinx.aws.lambda.dispatch

import net.kotlinx.json.gson.GsonData

/** 실패 이벤트 */
data class LambdaDispatcherFailEvent(val gsonData: GsonData, val e: Throwable) : AwsLambdaEvent