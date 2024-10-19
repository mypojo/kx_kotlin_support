package net.kotlinx.domain.batchStep.stepDefault

import aws.sdk.kotlin.services.s3.listObjectsV2
import com.lectra.koson.obj
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.aws.lambda.dispatch.LambdaDispatchLogic
import net.kotlinx.aws.lambda.dispatch.synch.S3LogicDispatcher
import net.kotlinx.aws.lambda.invokeAsynch
import net.kotlinx.aws.lambda.lambda
import net.kotlinx.aws.s3.s3
import net.kotlinx.aws.with
import net.kotlinx.calculator.ProgressData
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.domain.batchStep.BatchStepConfig
import net.kotlinx.domain.batchStep.BatchStepInput
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.serial.SerialJsonSet
import net.kotlinx.json.serial.SerialToJson
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.regex.RegexSet
import net.kotlinx.string.retainFrom
import net.kotlinx.time.TimeStart

/**
 * S3 리스팅후 동시성 한도까지만 동시에 호출 (모든 람다 트리거 = IP분산) -> 대기 반복.
 * S3가 다 삭제되면 처리 종료
 * 람다에에는 반드시 타임아웃이 걸려있어야함
 *
 * 1. 가능하면 쓰지말고
 * 2. 써야 한다면 list 구할때 해시 분할하게 작업할것!  ex) 10개중  1,3,5 ->2,4,6  이런식으로 처리해야 중복 방지가능
 *  */
class StepList : LambdaDispatchLogic {

    private val log = KotlinLogging.logger {}

    private val aws: AwsClient by koinLazy()
    private val config: BatchStepConfig by koinLazy()

    override suspend fun execute(input: GsonData): Any {

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
                    S3LogicDispatcher.KEY to it
                }
                log.trace { " -> lambdaInput $lambdaInput" }
                aws.lambda.with { invokeAsynch(this@StepList.config.lambdaFunctionName, lambdaInput) }
            }
        }.coroutineExecute(100) // 100개 정도는 문제 없음.

        log.info { "리스팅 종료. ${contents.size}건 -> $start" }

        val stepStart = StepStartContext.parseJson(input["option"]["stepStart"]["body"].toString())
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
) : SerialToJson {
    override fun toJson(): String = SerialJsonSet.JSON_OTHER.encodeToString(this)


}
