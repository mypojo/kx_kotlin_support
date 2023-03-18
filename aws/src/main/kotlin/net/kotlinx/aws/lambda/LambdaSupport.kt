package net.kotlinx.aws.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.invoke
import aws.sdk.kotlin.services.lambda.model.InvocationType
import aws.sdk.kotlin.services.lambda.model.InvokeResponse
import net.kotlinx.core1.string.ResultText
import net.kotlinx.core2.gson.GsonData


/**  파라메터 그대로 전달  */
suspend fun LambdaClient.invoke(functionName: String, param: GsonData, invocationType: InvocationType = InvocationType.Event): ResultText {
    val resp: InvokeResponse = this.invoke {
        this.functionName = functionName
        this.payload = param.toString().toByteArray()
        this.invocationType = invocationType
    }
    return when (resp.statusCode) {
        200 -> ResultText(true, resp.payload.contentToString())
        else -> ResultText(false, resp.functionError ?: "unknown")
    }
}