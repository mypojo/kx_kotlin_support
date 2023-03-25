package net.kotlinx.aws.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.invoke
import aws.sdk.kotlin.services.lambda.model.InvocationType
import aws.sdk.kotlin.services.lambda.model.InvokeResponse
import aws.sdk.kotlin.services.lambda.model.UpdateFunctionCodeResponse
import aws.sdk.kotlin.services.lambda.updateFunctionCode
import net.kotlinx.core1.string.ResultText
import net.kotlinx.core2.gson.GsonData


/**  파라메터 그대로 전달  */
suspend fun LambdaClient.invoke(functionName: String, param: GsonData, invocationType: InvocationType = InvocationType.Event): ResultText {
    val resp: InvokeResponse = this.invoke {
        this.functionName = functionName
        this.payload = param.toString().toByteArray()
        this.invocationType = invocationType
    }
    val ok = resp.functionError == null //이게 널이면 성공 (문서확인)
    return ResultText(ok, resp.payload?.let { String(it) } ?: "-") //결과코드는 무조건 200라인임. payload 변환 주의!. 비동기면 결과 없음
}

//==================================================== 제공 함수 (도커) ======================================================

/** 배포 후 이거 실행해주면 반영됨 */
suspend fun LambdaClient.updateFunctionCode(functionName: String, imageUri: String, tag: String): UpdateFunctionCodeResponse {
    return this.updateFunctionCode {
        this.functionName = functionName
        this.imageUri = "$imageUri:$tag"
    }
}