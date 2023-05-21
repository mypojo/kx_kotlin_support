package net.kotlinx.aws.module.batchStep.step

/**
 * 대량 처리 로직에서, 하나의 처리를 담당하는 클래스 정의
 *  */
interface StepLogicRuntime {
    suspend fun executeLogic(input: StepLogicInput): List<StepLogicOutput>
}