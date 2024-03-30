package net.kotlinx.aws.lambdaUrl

import net.kotlinx.aws.lambda.LambdaMapResult
import org.apache.http.HttpStatus

class LambdaUrlDedirectOutput(
    val location: String
) : LambdaMapResult {

    override fun toLambdaMap(): Map<String, Any> {
        return mapOf(
            "statusCode" to HttpStatus.SC_MOVED_TEMPORARILY, //302 고정
            "headers" to mapOf(
                "Location" to location
            ),
            "body" to "",
        )
    }
}