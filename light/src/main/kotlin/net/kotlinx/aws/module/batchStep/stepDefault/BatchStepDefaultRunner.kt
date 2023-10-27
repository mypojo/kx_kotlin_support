package net.kotlinx.aws.module.batchStep.stepDefault

import com.amazonaws.services.lambda.runtime.Context
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.core.gson.GsonData

/**
 * 배치스탬 기본 실행기
 * 페이로드에 아래처럼 설정(CDK)되어있어서 method 로 필터링 가능
 *         "Payload": {
 *           "method": "StepStart",
 *           "option.$": "$.option"
 *         }
 *
 *  이하 기본 제공되는 메소드들
 *         StepStart(config),
 *         StepList(config),
 *         StepEnd(config),
 *
 * */
class BatchStepDefaultRunner(logics: List<LambdaLogicHandler>) : LambdaLogicHandler {

    private val methodMap: Map<String, LambdaLogicHandler> = logics.associateBy { v -> v::class.simpleName!! } //Capital 그대로 사용한다

    /** 핸들러 실행 */
    override suspend fun invoke(input: GsonData, context: Context?): Any? {
        val methodName = input[AwsNaming.METHOD].str ?: return null
        val methodLogic = methodMap[methodName] ?: throw IllegalArgumentException("$methodName id not found")
        val methodResult = methodLogic(input, context)
        checkNotNull(methodResult)
        return methodResult
    }


}


