package net.kotlinx.openAi


/**
 * API 모델 & 코스트 정의
 * */
data class OpenAiModel(
    /** 오피셜 ID */
    val id: String,
    /** 토큰 1M 당 비용 ($) */
    val cost: Double,
)