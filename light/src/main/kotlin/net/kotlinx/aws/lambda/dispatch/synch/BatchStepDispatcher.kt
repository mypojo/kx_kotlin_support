package net.kotlinx.aws.lambda.dispatch.synch

import com.amazonaws.services.lambda.runtime.Context
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.lambda.dispatch.LambdaDispatch
import net.kotlinx.aws.lambda.dispatch.LambdaDispatchLogic
import net.kotlinx.domain.batchStep.BatchStepConfig
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.reflect.name

/**
 * 배치스탬 기본 실행기
 * 페이로드에 아래처럼 설정(CDK)되어있어서 method 로 필터링 가능
 *         "Payload": {
 *           "method": "StepStart",
 *           "option.$": "$.option"
 *         }
 * */
class BatchStepDispatcher : LambdaDispatch {

    private val batchStepConfig by koinLazy<BatchStepConfig>()

    /** 내부적으로 사용.decapital 로 네이밍해준다. */
    private val methodMap: Map<String, LambdaDispatchLogic> by lazy { batchStepConfig.logics.associateBy { v -> v::class.name() } }

    override suspend fun postOrSkip(input: GsonData, context: Context?): Any? {
        val methodName = input[AwsNaming.METHOD].str ?: return null
        val methodLogic = methodMap[methodName] ?: throw IllegalArgumentException("$methodName id not found")
        return methodLogic.execute(input)
    }


}


