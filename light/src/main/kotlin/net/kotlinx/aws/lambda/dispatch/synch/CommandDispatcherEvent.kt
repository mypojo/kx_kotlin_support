package net.kotlinx.aws.lambda.dispatch.synch

import net.kotlinx.aws.lambda.dispatch.AwsLambdaEvent
import net.kotlinx.json.gson.GsonData

/** 간단 커맨드 이벤트 */
data class CommandDispatcherEvent(val commandName: String, val gsonData: GsonData) : AwsLambdaEvent {
    /** 결과 */
    var output: Any? = null
}