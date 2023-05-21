package net.kotlinx.aws.module.batchStep.step

/**
 * SFN에서 호출되는 람다 메소드들
 *  */
sealed interface StepHandler {
    /** 람다 요청 처리 */
    suspend fun handleRequest(event: Map<String, Any>): Any

}
