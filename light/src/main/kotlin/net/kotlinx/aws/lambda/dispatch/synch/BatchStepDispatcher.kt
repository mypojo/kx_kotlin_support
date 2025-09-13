package net.kotlinx.aws.lambda.dispatch.synch

import com.amazonaws.services.lambda.runtime.Context
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.lambda.dispatch.LambdaDispatch
import net.kotlinx.domain.batchStep.BatchStepConfig
import net.kotlinx.domain.batchStep.BatchStepLogic
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koin
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

    /** 내부적으로 사용 */
    private val methodMap: Map<String, BatchStepLogic> by lazy { batchStepConfig.logics.associateBy { v -> v::class.name() } }

    override suspend fun postOrSkip(input: GsonData, context: Context?): Any? {
        val methodName = input[AwsNaming.METHOD].str ?: return null
        val methodLogic = methodMap[methodName] ?: run { koin<BatchStepLogic>(methodName) } //디폴트 외에는 인젝션 해준다
        return methodLogic.execute(input)
    }


}


