package net.kotlinx.aws.lambdaUrl

import net.kotlinx.aws.lambda.LambdaMapResult

/** 람다 결과  */
class LambdaUrlOutput(
    val body: String,
    val statusCode: Int = 200,
    val contentType: String = "text/html",
) : LambdaMapResult {

    override fun toLambdaMap(): Map<String, Any> {
        return mapOf(
            "statusCode" to statusCode,
            "headers" to mapOf(
                "content-type" to contentType
            ),
            "body" to body,
        )
    }
}
