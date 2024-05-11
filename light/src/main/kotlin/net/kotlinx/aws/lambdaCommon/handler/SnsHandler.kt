package net.kotlinx.aws.lambdaCommon.handler

import com.amazonaws.services.lambda.runtime.Context
import com.google.gson.JsonSyntaxException
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.json.gson.GsonData


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

        val sns = input["Sns"]

        /** JSON 그 자체가 메세지인 경우가 있고, 단순 텍스트(Budget Notification 등)인 경우도 있다 */
        val body = sns["Message"].str!!
        try {
            block(GsonData.parse(body))
        } catch (e: JsonSyntaxException) {
            //단순 문자열인경우 sns 그대로 전달
            block(sns)
        }
        return body
    }

    companion object {
        /** 이거 두문자가 소문자일수도 있나? */
        const val EVENT_SOURCE = "EventSource"
        const val SOURCE_SNS = "aws:sns"
    }

}