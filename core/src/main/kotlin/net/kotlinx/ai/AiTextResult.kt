package net.kotlinx.ai

import net.kotlinx.json.gson.ResultGsonData

/**
 * 간단 인터페이스
 * 배드락 기반으로 구현
 * */
data class AiTextResult(

    val model: AiModel,

    /**
     * 결과 데이터.
     * null인경우 프롬프트 오류
     * */
    val body: ResultGsonData,

    val inputTokens: Int,

    val outputTokens: Int,

    /** 걸린시간 밀리초 */
    val duration: Long,

    ) {


    /**
     * 금액($)
     *  @see printSimple  참고
     * */
    fun cost(): Double = model.cost(inputTokens, outputTokens)


}