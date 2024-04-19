package net.kotlinx.aws.lambdaCommon.handler

import com.amazonaws.services.lambda.runtime.Context
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.core.gson.GsonData


/**
 * SQS에 데이터 입력 -> 람다 트리거
 * ex) 특정 로직 실행..
 */
class SqsHandler(
    /**
     * SQS 본문 내용으로 트리거
     * */
    private val block: suspend (sqsBody: GsonData) -> Unit
) : LambdaLogicHandler {

    override suspend fun invoke(input: GsonData, context: Context?): Any? {
        if (input[EVENT_SOURCE].str != SOURCE_SQS) return null

        val body: String = input["body"].str ?: "{}"
        val sqsBody: GsonData = GsonData.parse(body)
        block(sqsBody)
        return "body"
    }

    companion object {
        const val EVENT_SOURCE = "eventSource"
        const val SOURCE_SQS = "aws:sqs"
    }


}
