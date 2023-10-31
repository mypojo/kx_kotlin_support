package net.kotlinx.aws.module.batchStep.stepDefault

import com.amazonaws.services.lambda.runtime.Context
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.core.gson.GsonData
import net.kotlinx.reflect.name

/**
 * 배치스탬 기본 실행기
 * 페이로드에 아래처럼 설정(CDK)되어있어서 method 로 필터링 가능
 *         "Payload": {
 *           "method": "StepStart",
 *           "option.$": "$.option"
 *         }
 * */
class BatchStepDefaultRunner(block: BatchStepDefaultRunner.() -> Unit = {}) : LambdaLogicHandler {

    /** 등록된 로직 */
    var logics: List<LambdaLogicHandler> = listOf(
        StepStart(),
        StepList(),
        StepEnd(),
    )

    /** 내부적으로 사용.decapital 로 네이밍해준다. */
    private val methodMap: Map<String, LambdaLogicHandler> by lazy { logics.associateBy { v -> v::class.name() } }

    /** 핸들러 실행 */
    override suspend fun invoke(input: GsonData, context: Context?): Any? {
        val methodName = input[AwsNaming.METHOD].str ?: return null
        val methodLogic = methodMap[methodName] ?: throw IllegalArgumentException("$methodName id not found")
        val methodResult = methodLogic(input, context)
        checkNotNull(methodResult)
        return methodResult
    }

    init {
        apply(block)
    }


}


