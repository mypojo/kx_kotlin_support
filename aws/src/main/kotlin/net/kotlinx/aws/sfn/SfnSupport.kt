package net.kotlinx.aws.sfn

import aws.sdk.kotlin.services.sfn.SfnClient
import aws.sdk.kotlin.services.sfn.model.StartExecutionResponse
import aws.sdk.kotlin.services.sfn.startExecution
import java.util.*

/** 시작 호출 */
suspend fun SfnClient.startExecution(awsId: String, stateMachineName: String, json: String, uuid: String = UUID.randomUUID().toString()): StartExecutionResponse {
    return this.startExecution {
        this.input = json
        this.name = uuid
        this.stateMachineArn = SfnUtil.stateMachineArn(awsId, stateMachineName, uuid)
    }
}