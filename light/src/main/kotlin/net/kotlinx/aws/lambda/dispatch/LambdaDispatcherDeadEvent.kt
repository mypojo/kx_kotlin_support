package net.kotlinx.aws.lambda.dispatch

import net.kotlinx.json.gson.GsonData

/** 매칭 결과가 하나도 없을때 이벤트 */
data class LambdaDispatcherDeadEvent(val gsonData: GsonData) : AwsLambdaEvent