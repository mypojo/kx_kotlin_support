package net.kotlinx.aws.module.batchStep.stepDefault

import aws.sdk.kotlin.services.s3.listObjectsV2
import com.amazonaws.services.lambda.runtime.Context
import com.lectra.koson.obj
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.aws.lambda.invokeAsynch
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.aws.lambdaCommon.handler.s3.S3LogicHandler
import net.kotlinx.aws.module.batchStep.BatchStepConfig
import net.kotlinx.aws.module.batchStep.BatchStepInput
import net.kotlinx.aws.with
import net.kotlinx.core.calculator.ProgressData
import net.kotlinx.core.concurrent.coroutineExecute
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.regex.RegexSet
import net.kotlinx.core.serial.SerialJsonObj
import net.kotlinx.core.serial.SerialJsonSet
import net.kotlinx.core.string.retainFrom
import net.kotlinx.core.time.TimeStart
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * S3 리스팅후 동시성 한도까지만 동시에 호출 (모든 람다 트리거 = IP분산) -> 대기 반복.
 * S3가 다 삭제되면 처리 종료
 * 람다에에는 반드시 타임아웃이 걸려있어야함
 *  */
class StepList : LambdaLogicHandler, KoinComponent {

    private val log = KotlinLogging.logger {}

    private val aws: AwsClient1 by inject()
    private val config: BatchStepConfig by inject()

    override suspend fun invoke(input: GsonData, context: Context?): Any {

        log.trace { "start.." }
        val start = TimeStart()
        val stepInput = BatchStepInput.parseJson(input.toString())
        val option = stepInput.option
        val listOption = option.listOption!!

        val prefix = "${config.workUploadInputDir}${option.targetSfnId}/"

        log.trace { "리스팅.." }
        val contents = aws.s3.with {
            listObjectsV2 {
                this.bucket = this@StepList.config.workUploadBuket
                this.prefix = prefix
                this.maxKeys = listOption.maxConcurrency //최대치 까지만 읽어서 실행
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

        log.info { "리스팅 종료. ${contents.size}건 -> $start" }

        val stepStart = option.stepStart!!
        val currentNum = contents.firstOrNull()?.substringAfterLast("/")?.retainFrom(RegexSet.NUMERIC)?.toInt() ?: stepStart.first
        return ListFireAndForgetContext(
            when {
                contents.isEmpty() -> LambdaUtil.OK
                else -> AwsNaming.CHOICE_RETRY
            },
            contents.size,
            currentNum,
            when {
                contents.isEmpty() -> "종료"
                else -> ProgressData(stepStart.total, currentNum - stepStart.first, stepStart.startTime).toString()
            },
        )
    }

}


@Serializable
data class ListFireAndForgetContext(
    /** 상태 */
    val state: String,
    /** 이번회차 처리 */
    val size: Int,
    /** 이번회차 최초 */
    val firstNum: Int?,
    /** 진행상태 */
    val progress: String,
) : SerialJsonObj {
    override fun toJson(): String = SerialJsonSet.KSON.encodeToString(this)
}
