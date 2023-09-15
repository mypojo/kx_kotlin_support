package net.kotlinx.aws.module.batchStep.stepDefault

import com.amazonaws.services.lambda.runtime.Context
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.aws.module.batchStep.BatchStepConfig
import net.kotlinx.aws.sfn.SfnUtil
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.string.toTextGrid
import net.kotlinx.core.time.toTimeString


class StepEnd(
    private val config: BatchStepConfig,
) : LambdaLogicHandler {

    private val log = KotlinLogging.logger {}

    override suspend fun invoke(input: GsonData, context: Context?): Any? {

        val context = BatchStepContext(input)
        val sfnId = context.option[SfnUtil.SFN_ID].str!!
        val query = """
                    SELECT file_name, COUNT(1) CNT,max(total_interval) total_interval,avg(interval) avg_interval,sum(length(output)) length
                    FROM d.batch_step
                    where sfn_id = '$sfnId'
                    group by file_name
                """
        val lines = config.athenaModule.readAll(query)
        val header = lines[0]
        val datas = lines.subList(1, lines.size).map { it.toTypedArray() }
        return if (datas.isEmpty()) {
            "데이터가 없습니다"
        } else {
            header.toTextGrid(datas).print()

            val sumOfInterval = datas.sumOf { it[2].toLong() }
            val cost = sumOfInterval / 1000 * LambdaUtil.COST_GI_PER_SEC / 4 * 1350
            log.info { "WAS lambda 과금 ${cost}원" }
            obj {
                "lambda-누적시간" to sumOfInterval.toTimeString()
                "lambda-누적시간(비용)" to "${cost}원"
                "lambda-개별평군처리시간" to datas.map { it[3].toDouble() }.average().toLong().toTimeString()
                "결과데이터 문자열수" to datas.sumOf { it[4].toLong() }
            }
        }
    }

}

