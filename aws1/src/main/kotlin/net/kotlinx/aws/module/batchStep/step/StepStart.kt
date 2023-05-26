package net.kotlinx.aws.module.batchStep.step

import aws.sdk.kotlin.services.s3.paginators.listObjectsV2Paginated
import mu.KotlinLogging
import net.kotlinx.aws.module.batchStep.BatchStepConfig
import net.kotlinx.aws.module.batchStep.BatchStepContext
import net.kotlinx.aws.module.batchStep.BatchStepMode
import net.kotlinx.core.concurrent.collectToList
import net.kotlinx.core.regex.RegexSet
import net.kotlinx.core.string.retainFrom
import java.time.LocalDateTime

/**
 * 시작시 메타데이터 처리
 *  */
class StepStart(
    private val config: BatchStepConfig,
) : StepHandler {

    private val log = KotlinLogging.logger {}

    override suspend fun handleRequest(event: Map<String, Any>): Any {
        val context = BatchStepContext(event)

        val paginated = config.aws.s3.listObjectsV2Paginated {
            this.bucket = config.workUploadBuket
            this.prefix = "${config.workUploadInputDir}${context.optionInput.targetSfnId}/"
        }

        val datas = paginated.collectToList { v -> v.contents?.map { StepStartS3Data(it.key!!) } ?: emptyList() }.flatten()

        return when (context.mode) {
            BatchStepMode.Map -> {
                return StepStartContext(
                    LocalDateTime.now(),
                    datas.size,
                    datas.first().Key.substringAfterLast("/").retainFrom(RegexSet.NUMERIC).toInt(),
                    datas //전체 데이터 리스트를 넣어준다.
                )
            }

            BatchStepMode.List -> {
                return StepStartContext(
                    LocalDateTime.now(),
                    datas.size,
                    datas.first().Key.substringAfterLast("/").retainFrom(RegexSet.NUMERIC).toInt(),
                    emptyList()
                )
            }

            else -> throw IllegalStateException("mode 가 없습니다. $event")
        }

    }

}

data class StepStartContext(
    val startTime: LocalDateTime,
    val total: Int,
    val first: Int,
    val datas: List<StepStartS3Data>,
)

@Suppress("PropertyName")
data class StepStartS3Data(val Key: String)