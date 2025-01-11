package net.kotlinx.ai

/**
 * 간단 인터페이스
 * 배드락 기반으로 구현
 * */
interface AiTextClient {

    val model: AiModel

    /**
     * 기본 함수
     * */
    suspend fun invokeModel(messages: List<Any>): AiTextResult


    //==================================================== 편의 메소드 ======================================================

    /**
     * 간단하게 채팅 입력해서 결과 비교용
     * */
    suspend fun chat(msg: String): AiTextResult = invokeModel(listOf(msg))

}