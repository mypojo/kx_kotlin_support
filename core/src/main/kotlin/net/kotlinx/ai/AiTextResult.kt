package net.kotlinx.ai

import net.kotlinx.exception.toSimpleString
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.ResultGsonData

/**
 * 간단 인터페이스
 * 배드락 기반으로 구현
 * */
data class AiTextResult(

    /** 모델 정보 */
    val model: AiModel,

    /**
     * 입력 데이터
     * */
    val input: List<AiTextInput>,

    /**
     * 결과 데이터.
     * null인경우 프롬프트 오류
     * */
    val output: ResultGsonData,

    val inputTokens: Int,

    val outputTokens: Int,

    /** 걸린시간 밀리초 */
    val duration: Long,

    ) {

    /** 로깅등, 특정 작업 구분용 마커 */
    var name: String = ""

    /** 예외 */
    var exception: Exception? = null

    /**
     * 금액($)
     *  @see printSimple  참고
     * */
    fun cost(): Double = model.cost(inputTokens, outputTokens)

    companion object {

        /** 공용 예외 처리 */
        fun fail(model: AiModel, input: List<AiTextInput>, e: Exception): AiTextResult {
            return AiTextResult(model, input, ResultGsonData(false, GsonData.parse(e.toSimpleString())), 0, 0, 0).apply {
                this.exception = e
            }
        }

    }


}