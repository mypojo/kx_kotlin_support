package net.kotlinx.ai

import net.kotlinx.core.Kdsl

/**
 * 간단 모델 정보
 * 요금은 달러임
 * */
class AiModel {

    @Kdsl
    constructor(block: AiModel.() -> Unit = {}) {
        apply(block)
    }

    /** 제공하는 벤더 */
    lateinit var group: String

    lateinit var id: String

    /** 간단 한글 이름 */
    lateinit var name: String

    //==================================================== 비용 관련 ======================================================

    /** 호출 1000 회당 요금 (퍼플렉시티) */
    var costOfRequest: Double = 0.0

    /** 1000개당 요금. 1M당 요금의 경우 1000을 나눠서 곱해야함 */
    var costOfInputToken: Double = 0.0

    /** 1000개당 요금 */
    var costOfOutputToken: Double = 0.0

    /** 요금 */
    fun cost(inputTokens: Int, outputTokens: Int): Double = (costOfRequest + inputTokens * costOfInputToken + outputTokens * costOfOutputToken) / 1000


}