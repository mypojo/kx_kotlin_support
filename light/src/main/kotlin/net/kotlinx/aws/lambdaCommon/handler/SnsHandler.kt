package net.kotlinx.aws.lambdaCommon.handler

import com.amazonaws.services.lambda.runtime.Context
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.core.gson.GsonData


/**
 * SNS에 데이터 입력 -> 람다 트리거
 * ex) 관리자 슬랙알람 등
 */
class SnsHandler(
    /**
     * SNS 본문 내용으로 트리거
     * */
    private val block: suspend (body: GsonData) -> Unit
) : LambdaLogicHandler {

    override suspend fun invoke(input: GsonData, context: Context?): Any? {
        if (input["EventSource"].str != EVENT_SOURCE) return null

        val body = GsonData.fromObj(input)
        block(body)
        return body
    }

    companion object {
        private const val EVENT_SOURCE = "aws:sns"
    }

}