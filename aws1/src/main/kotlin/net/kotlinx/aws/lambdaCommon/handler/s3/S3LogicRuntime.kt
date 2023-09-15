package net.kotlinx.aws.lambdaCommon.handler.s3

/**
 * 대량 처리 로직에서, 하나의 처리를 담당하는 클래스 정의
 *  */
interface S3LogicRuntime {
    suspend fun executeLogic(input: S3LogicInput): List<S3LogicOutput>
}