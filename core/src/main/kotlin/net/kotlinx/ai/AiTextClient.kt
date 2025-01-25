package net.kotlinx.ai

/**
 * 간단 인터페이스
 * 배드락 기반으로 구현
 * 1회성 요청에 대해서만 사용한다
 *
 * 참고용어
 * # Text : 1회성 요청 / 응답
 * # Chat : 채팅
 * */
interface AiTextClient {

    val model: AiModel

    /** 간단하게 1회성으로 채팅 입력 */
    suspend fun text(input: List<AiTextInput>): AiTextResult

    /** 간단하게 1회성으로 채팅 입력 */
    suspend fun text(input: String): AiTextResult = text(listOf(AiTextInput.AiTextString(input)))

}