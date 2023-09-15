package net.kotlinx.aws.module.batchStep.stepDefault

import aws.sdk.kotlin.services.s3.listObjectsV2
import com.amazonaws.services.lambda.runtime.Context
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.aws.lambda.invokeAsynch
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.aws.lambdaCommon.handler.s3.S3LogicHandler
import net.kotlinx.aws.module.batchStep.BatchStepConfig
import net.kotlinx.aws.with
import net.kotlinx.core.calculator.ProgressData
import net.kotlinx.core.concurrent.coroutineExecute
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.regex.RegexSet
import net.kotlinx.core.string.retainFrom
import net.kotlinx.core.time.TimeStart

/**
 * S3 리스팅후 동시성 한도까지만 동시에 호출 (모든 람다 트리거 = IP분산) -> 대기 반복.
 * S3가 다 삭제되면 처리 종료
 * 람다에에는 반드시 타임아웃이 걸려있어야함
 *  */
class StepList(
    private val config: BatchStepConfig,
) : LambdaLogicHandler {

    val aws = config.aws

    private val log = KotlinLogging.logger {}

    override suspend fun invoke(input: GsonData, context: Context?): Any? {

        log.trace { "start.." }
        val start = TimeStart()
        val context = BatchStepContext(input)
        val input = context.optionInput

        val prefix = "${config.workUploadInputDir}${input.targetSfnId}/"

        log.trace { "리스팅.." }
        val contents = aws.s3.with {
            listObjectsV2 {
                this.bucket = this@StepList.config.workUploadBuket
                this.prefix = prefix
                this.maxKeys = input.maxConcurrency //최대치 까지만 읽어서 실행
            }.contents?.map { it.key!! } ?: emptyList()
        }
        log.trace { " -> ${config.workUploadBuket}/$prefix -> S3 list ${contents.size}건" }

        contents.map {
            suspend {
                //AWS의 S3 입력과 동일하게 맞춰준다.
                val lambdaInput = obj {
                    S3LogicHandler.KEY to it
                }
                log.trace { " -> lambdaInput $lambdaInput" }
                aws.lambda.with { invokeAsynch(this@StepList.config.lambdaFunctionName, lambdaInput) }
            }
        }.coroutineExecute(100) // 100개 정도는 문제 없음.


        val result = context.option[this::class.simpleName!!]
        log.info { "리스팅 종료. ${contents.size}건 -> 걸린시간 : $start / $result" }

        val state = when {
            contents.isEmpty() -> LambdaUtil.OK
            result.empty -> AwsNaming.CHOICE_FIRST
            else -> AwsNaming.CHOICE_RETRY
        }

        val stepStart: StepStartContext = context[StepStart::class]
        val currentNum = contents.firstOrNull()?.substringAfterLast("/")?.retainFrom(RegexSet.NUMERIC)?.toInt() ?: stepStart.first
        val progressData = when {
            contents.isEmpty() -> "종료"
            else -> ProgressData(stepStart.total, currentNum - stepStart.first, stepStart.startTime).toString()
        }
        return ListFireAndForgetContext(state, contents.size, currentNum, progressData)
    }

}

data class ListFireAndForgetContext(
    /** 상태 */
    val state: String,
    /** 이번회차 처리 */
    val size: Int,
    /** 이번회차 최초 */
    val firstNum: Int?,
    /** 진행상태 */
    val progress: String,
)

