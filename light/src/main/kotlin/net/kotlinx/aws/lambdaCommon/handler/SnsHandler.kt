package net.kotlinx.aws.lambdaCommon.handler

import com.amazonaws.services.lambda.runtime.Context
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.core.gson.GsonData


/**
 * SNS에 데이터 입력 -> 람다 트리거
 * ex) 관리자 슬랙알람 등
 * 이벤트 스키마 dto 매핑 쓰지 않는다.  -> java8 & 잭슨이라서 좋지않다.
 */
class SnsHandler(
    /**
     * SNS 본문 내용으로 트리거
     * */
    private val block: suspend (body: GsonData) -> Unit
) : LambdaLogicHandler {

    override suspend fun invoke(input: GsonData, context: Context?): Any? {
        if (input[EVENT_SOURCE].str != SOURCE_SNS) return null

        val body = GsonData.fromObj(input)
        block(body)
        return body
    }

    companion object {
        /** 이거 두문자가 소문자일수도 있나? */
        const val EVENT_SOURCE = "EventSource"
        const val SOURCE_SNS = "aws:sns"
    }

}