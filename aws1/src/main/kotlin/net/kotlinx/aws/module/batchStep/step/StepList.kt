package net.kotlinx.aws.module.batchStep.step

import aws.sdk.kotlin.services.s3.listObjectsV2
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.aws.module.batchStep.BatchStepConfig
import net.kotlinx.aws.module.batchStep.BatchStepContext
import net.kotlinx.aws1.AwsNaming
import net.kotlinx.aws1.lambda.LambdaUtil
import net.kotlinx.aws1.lambda.invokeAsynch
import net.kotlinx.aws1.with
import net.kotlinx.core1.regex.RegexSet
import net.kotlinx.core1.string.retainFrom
import net.kotlinx.core1.time.TimeStart
import net.kotlinx.core2.calculator.ProgressData
import net.kotlinx.core2.concurrent.coroutineExecute

/**
 * S3 리스팅후 동시성 한도까지만 동시에 호출 (모든 람다 트리거 = IP분산) -> 대기 반복.
 * S3가 다 삭제되면 처리 종료
 * 람다에에는 반드시 타임아웃이 걸려있어야함
 *  */
class StepList(
    private val bsConfig: BatchStepConfig,
) : StepHandler {

    val aws = bsConfig.aws

    private val log = KotlinLogging.logger {}

    override suspend fun handleRequest(event: Map<String, Any>): Any {

        log.trace { "start.." }
        val start = TimeStart()
        val context = BatchStepContext(event)
        val input = context.optionInput

        val prefix = "${bsConfig.workUploadInputDir}${input.targetSfnId}/"

        log.trace { "리스팅.." }
        val contents = aws.s3.with {
            listObjectsV2 {
                this.bucket = bsConfig.workUploadBuket
                this.prefix = prefix
                this.maxKeys = input.maxConcurrency //최대치 까지만 읽어서 실행
            }.contents?.map { it.key!! } ?: emptyList()
        }
        log.trace { " -> ${bsConfig.workUploadBuket}/$prefix -> S3 list ${contents.size}건" }

        contents.map {
            suspend {
                //AWS의 S3 입력과 동일하게 맞춰준다.
                val lambdaInput = obj {
                    StepLogic.key to it
                }
                log.trace { " -> lambdaInput $lambdaInput" }
                aws.lambda.with { invokeAsynch(bsConfig.lambdaFunctionName, lambdaInput) }
            }
        }.coroutineExecute(100) // 100개 정도는 문제 없음.


        val result = context.option[this::class.simpleName!!]
        log.info { "리스팅 종료. ${contents.size}건 -> 걸린시간 : $start / $result" }

        val state = when {
            contents.isEmpty() -> LambdaUtil.Ok
            result.empty -> AwsNaming.choiceFirst
            else -> AwsNaming.choiceRetry
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

