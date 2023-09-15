package net.kotlinx.aws.module.batchStep.stepDefault

import aws.sdk.kotlin.services.s3.paginators.listObjectsV2Paginated
import com.amazonaws.services.lambda.runtime.Context
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.aws.lambdaCommon.handler.s3.S3LogicHandler
import net.kotlinx.aws.module.batchStep.BatchStepConfig
import net.kotlinx.aws.module.batchStep.BatchStepMode
import net.kotlinx.aws.s3.toList
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.regex.RegexSet
import net.kotlinx.core.string.retainFrom
import java.time.LocalDateTime

/**
 * 시작시 메타데이터 처리
 *  */
class StepStart(
    private val config: BatchStepConfig,
) : LambdaLogicHandler {

    private val log = KotlinLogging.logger {}

    override suspend fun invoke(input: GsonData, context: Context?): Any? {

        val context = BatchStepContext(input)

        val datas = config.aws.s3.listObjectsV2Paginated {
            this.bucket = config.workUploadBuket
            this.prefix = "${config.workUploadInputDir}${context.optionInput.targetSfnId}/"
        }.toList()

        return when (context.mode) {
            BatchStepMode.Map -> {
                StepStartContext(
                    LocalDateTime.now(),
                    datas.size,
                    datas.first().substringAfterLast("/").retainFrom(RegexSet.NUMERIC).toInt(),
                    datas.map {
                        obj {
                            S3LogicHandler.KEY to it
                        }.toString()
                    } //전체 데이터 리스트를 넣어준다. -> sfn에서 읽어서 event로 전달해줌
                )
            }

            BatchStepMode.List -> {
                StepStartContext(
                    LocalDateTime.now(),
                    datas.size,
                    datas.first().substringAfterLast("/").retainFrom(RegexSet.NUMERIC).toInt(),
                    emptyList()
                )
            }

            else -> throw IllegalStateException("mode 가 없습니다. $input")
        }

    }

}

/** 로그로 남길 기록 */
data class StepStartContext(
    /** 시작시간 */
    val startTime: LocalDateTime,
    /** 이번로드 전체수 */
    val total: Int,
    /** 이번로드 첫 파일 넘버 */
    val first: Int,
    /**
     * MAP인경우 전체 데이터의 S3 key (SFN에서 이 경로로 읽어감)
     * json 형식이어야함  {Key:...}
     *  */
    val datas: List<String>,
)
