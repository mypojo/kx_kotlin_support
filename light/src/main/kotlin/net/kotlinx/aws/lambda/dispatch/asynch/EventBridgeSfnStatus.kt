package net.kotlinx.aws.lambda.dispatch.asynch

/**
 * SFN 상태 변경(성공/실패 ..) 이벤트 전달
 * */
data class EventBridgeSfnStatus(private val event: EventBridgeJson) : EventBridge by event {
    val stateMachineArn = detail["stateMachineArn"].str!!
    val stateMachineName = stateMachineArn.substringAfterLast(":")

    val executionArn = detail["executionArn"].str!!

    /** SFN ID */
    val name = detail["name"].str!!
    val status = detail["status"].str!!
    val startDate = detail["startDate"].str!!.toLong()
    val input = detail["input"].str ?: "-"
    val output = detail["output"].str ?: "-"
    val cause = detail["cause"].str ?: "-"
}
