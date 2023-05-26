package net.kotlinx.aws.module.batchStep

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.lambda.LambdaHandlerUtil
import net.kotlinx.aws.module.batchStep.step.*

/**
 * S3 베이스의 설정파일
 * */
class BatchStepRunner(
    config: BatchStepConfig
) {

    /** 외부 접근 가능 */
    val stepLogic: StepLogic = StepLogic(config)
    private val log = KotlinLogging.logger {}

    private val methodMap: Map<String, StepHandler> = listOf(
        stepLogic,
        //이하 기본 제공되는 메소드들
        StepStart(config),
        StepEnd(config),
        StepList(config),
    ).associateBy { v -> v::class.simpleName!! } //Capital 그대로 사용한다

    /** 핸들러 실행 */
    fun handleRequest(event: Map<String, Any>): Map<String, Any> {
        val methodName = event[AwsNaming.method]
        val method = methodName?.let {
            methodMap[it] ?: throw IllegalArgumentException("$it id not found")
        } ?: stepLogic //없으면 기본으로 로직으로 간주

        return runBlocking {
            log.debug { " -> ${method::class.simpleName} 실행" }
            val handlerResp = method.handleRequest(event)
            return@runBlocking LambdaHandlerUtil.anyToLambdaMap(handlerResp)
        }
    }


}


