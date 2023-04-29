package net.kotlinx.module1.aws.lambda.snsHandler

import net.kotlinx.core1.collection.invokeUntilNotNull
import net.kotlinx.core2.gson.GsonData

/**
 * 람다에서 직접 json을 받아서 SNS를 처리하는 템플릿
 * */
class LambdaSnsHandler(
    private val handlers: List<(GsonData) -> String?> = defaultHandlers
) : (GsonData) -> String? {

    companion object {
        private const val EVENT_SOURCE = "aws:sns"

        /** 기본 핸들러 */
        val defaultHandlers: List<(GsonData) -> String?> by lazy {
            listOf(
                LambdaSnsCloudwatch(),
                LambdaSnsCodePipeline(),
                LambdaSnsEcsHealthFail(),
                LambdaSnsSfnFail(),
                LambdaSnsMatchFail(), //마지막에는 실패 전송
            )
        }
    }

    /** SNS는 여러간 한번에 올 수 있음 */
    override fun invoke(eventGson: GsonData): String? {
        return eventGson["Records"].let { records ->
            if (records.empty) {
                doInvoke(eventGson) //단일 요청
            } else {
                records.map { doInvoke(it) }.joinToString(",") //복수건 요청
            }
        }
    }

    private fun doInvoke(eventGson: GsonData): String? {
        val eventSource: String = eventGson["EventSource"].str ?: return null //ㅅㅂ 대문자임
        if (eventSource != EVENT_SOURCE) return null

        //모든 SNS는 아래 로직을 따름
        val sns = eventGson["Sns"]
        val msg = GsonData.parse(sns["Message"].str) //여기에 실제 메세지가 들어있음
        return handlers.invokeUntilNotNull(msg)
    }
}