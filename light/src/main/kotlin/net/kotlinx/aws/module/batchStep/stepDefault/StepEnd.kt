package net.kotlinx.aws.module.batchStep.stepDefault

import com.amazonaws.services.lambda.runtime.Context
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.athena.AthenaModule
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.aws.lambdaCommon.LambdaLogicHandler
import net.kotlinx.aws.module.batchStep.BatchStepConfig
import net.kotlinx.aws.module.batchStep.BatchStepInput
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.time.toTimeString
import net.kotlinx.module.job.Job
import net.kotlinx.module.job.JobRepository
import net.kotlinx.module.job.JobStatus
import net.kotlinx.module.job.JobUpdateSet
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDateTime


class StepEnd : LambdaLogicHandler, KoinComponent {

    private val log = KotlinLogging.logger {}

    private val aws1: AwsClient1 by inject()
    private val config: BatchStepConfig by inject()
    private val athenaModule: AthenaModule by inject()
    private val jobRepository: JobRepository by inject()

    override suspend fun invoke(input: GsonData, context: Context?): Any {

        val option = BatchStepInput.parseJson(input.toString()).option

        val inputDatas = config.listInputs(option.targetSfnId)
        if (inputDatas.isNotEmpty()) {
            throw IllegalStateException("StepEnd 감지 : 처리되지 못한 데이터파일 ${inputDatas.size}건")
        }

        val datas = athenaModule.readAll {
            """
                SELECT COUNT(1) CNT,sum(total_interval) total_interval,avg(total_interval) avg_interval,sum(total_size) total_size
                FROM batch_step
                where sfn_id = '${option.sfnId}'
                """
        }.drop(1)[0]

        val resultJson = when {
            datas.isEmpty() -> obj {
                throw IllegalStateException("결과 데이터가 존재하지 않습니다!!")
            }

            else -> {
                val fileCnt = datas[0].toLong()
                val sumOfInterval = datas[1].toLong()
                val avgOfInterval = datas[2].toDouble()
                val totalCnt = datas[3].toLong()
                val cost = sumOfInterval / 1000 * LambdaUtil.COST_GI_PER_SEC / 4 * 1350
                log.info { "WAS lambda 과금 ${cost}원" }
                obj {
                    "데이터크기" to "${totalCnt}건"
                    "분할파일" to "${fileCnt}개"
                    "누적시간합계" to sumOfInterval.toTimeString()
                    "평균처리시간" to avgOfInterval.toLong().toTimeString()
                    "람다비용(시간)" to "${cost}원"
                }
            }
        }

        jobRepository.getItem(Job(option.jobPk, option.jobSk))!!.apply {
            jobStatus = JobStatus.SUCCEEDED
            endTime = LocalDateTime.now()
            jobContext = resultJson.toString()
            jobRepository.updateItem(this, JobUpdateSet.END)
            log.debug { "job [${this.toKeyString()}] 로그 update" }
        }

        return resultJson
    }

}

