package net.kotlinx.aws1.batchStep

/**
 * 대량 처리 로직에서, 하나의 처리를 담당하는 클래스 정의
 *  */
interface BatchStepRuntime {
    suspend fun executeEach(input: Any): String
}