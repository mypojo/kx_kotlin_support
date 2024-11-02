package net.kotlinx.domain.batchStep.stepDefault

import com.lectra.koson.ObjectType
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.athena.AthenaModule
import net.kotlinx.aws.lambda.LambdaUtil
import net.kotlinx.aws.lambda.dispatch.LambdaDispatchLogic
import net.kotlinx.domain.batchStep.BatchStepCallback
import net.kotlinx.domain.batchStep.BatchStepConfig
import net.kotlinx.domain.batchStep.BatchStepParameter
import net.kotlinx.domain.job.Job
import net.kotlinx.domain.job.JobRepository
import net.kotlinx.domain.job.JobStatus
import net.kotlinx.domain.job.JobUpdateSet
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.koson.toGsonData
import net.kotlinx.koin.Koins
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.retry.RetryTemplate
import net.kotlinx.time.toTimeString
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds


/** 공통 처리 */
class StepEnd : LambdaDispatchLogic {

    private val log = KotlinLogging.logger {}

    private val aws: AwsClient by koinLazy()
    private val config: BatchStepConfig by koinLazy()
    private val athenaModule: AthenaModule by koinLazy()
    private val jobRepository: JobRepository by koinLazy()

    var retry = RetryTemplate {
        interval = 3.seconds
        predicate = RetryTemplate.ALL
    }

    override suspend fun execute(input: GsonData): Any {

        val option = BatchStepParameter.parseJson(input.toString()).option

        val inputDatas = config.listInputs(option.targetSfnId)
        if (inputDatas.isNotEmpty()) {
            throw IllegalStateException("StepEnd 감지 : 처리되지 못한 데이터파일 ${inputDatas.size}건")
        }

        val job = jobRepository.getItem(Job(option.jobPk, option.jobSk))!!

        log.trace { "BatchStepCallback 이 등록되어있다면 실행" }
        Koins.koinOrNull<BatchStepCallback>(job.pk)?.let {
            it.execute(option, job)
        }

        val datas = retry.withRetry {
            athenaModule.readAll {
                """
                SELECT COUNT(1) CNT,sum(total_interval) total_interval,avg(total_interval) avg_interval,sum(total_size) total_size
                FROM batch_step
                where sfn_id = '${option.targetSfnId}'
                """
            }.drop(1)[0]
        }

        val resultJson: ObjectType = when {
            datas.isEmpty() -> obj {
                throw IllegalStateException("결과 데이터가 존재하지 않습니다!!")
            }

            else -> {
                val fileCnt = datas[0].toLong()
                if (fileCnt == 0L) {
                    obj {
                        "데이터크기" to "0건"
                    }
                } else {
                    val sumOfInterval = datas[1].toLong()
                    val avgOfInterval = datas[2].toDouble()
                    val totalCnt = datas[3].toLong()
                    val cost = LambdaUtil.cost(sumOfInterval)
                    log.info { "WAS lambda 과금 ${cost}원" }
                    obj {
                        "데이터크기" to "${totalCnt}건"
                        "분할파일" to "${fileCnt}개"
                        "누적시간합계" to sumOfInterval.toTimeString()
                        "평균처리시간" to avgOfInterval.toLong().toTimeString()
                        "람다비용(시간)" to "${cost.toLong()}원"
                    }
                }
            }
        }

        job.apply {
            jobStatus = JobStatus.SUCCEEDED
            endTime = LocalDateTime.now()
            jobContext = resultJson.toGsonData()
            jobRepository.updateItem(this, JobUpdateSet.END)
            log.debug { "job [${this.toKeyString()}] 로그 update" }
        }

        return resultJson
    }

}

