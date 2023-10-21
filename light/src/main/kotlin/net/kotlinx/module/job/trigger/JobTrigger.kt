package net.kotlinx.module.job.trigger

import mu.KotlinLogging
import net.kotlinx.aws.AwsClient1
import net.kotlinx.core.lib.toSimpleString
import net.kotlinx.module.job.*
import net.kotlinx.module.job.define.JobDefinitionRepository
import net.kotlinx.module.job.define.JobExecuteType
import java.time.LocalDateTime


/**
 * 잡을 원격으로 실행시킨다. -> 해당 원격 요청은 각 로컬 머신에서 jobRunner 로 실행된다.
 * 잡 트리거 환경 상세 정의. 미리 정의되어서 잡 정의에 포함됨
 * ex) lambda & 메소드명
 * ex) batch & vcpu 수
 */
class JobTrigger(
    private val jobFactory: JobFactory,
    val jobRepository: JobRepository,
    val jobSerializer: JobSerializer,
    val aws: AwsClient1,
    val jobLocalExecutor: JobLocalExecutor,
    val jobDefinitionRepository: JobDefinitionRepository,
) {

    private val log = KotlinLogging.logger {}

    suspend fun trigger(pk: String): Job {
        val jobDefinition = jobDefinitionRepository.findById(pk)
        return trigger(jobDefinition.toJobOption())
    }

    /** 실제 원격 트리거시 */
    suspend fun trigger(op: JobTriggerOption): Job {

        val job: Job = jobFactory.create(op.jobDefinition.jobPk)
        job.jobExeFrom = op.jobExeFrom
        job.jobOption = op.jobOption.toString()
        job.persist = op.jobDefinition.jobExecuteType == JobExecuteType.NORMAL

        //특수한 경우 예외 처리해줌
        if (op.jobDefinition.jobExecuteType == JobExecuteType.LAMBDA_SYNCH_NOLOG) {
            job.persist = false
            op.jobTriggerMethod = JobTriggerMethod.LOCAL
            log.debug { "  --> job(${job.toKeyString()}) 실행 (${JobExecuteType.LAMBDA_SYNCH_NOLOG})" }
        }

        jobRepository.putItem(job)
        return try {
            op.jobTriggerMethod.trigger(op, this, job)
            job
        } catch (e: Throwable) {

            job.jobStatus = JobStatus.FAILED
            job.jobErrMsg = e.toSimpleString()
            job.endTime = LocalDateTime.now()

            log.warn { "잡 트리거중 예외!!" }
            jobRepository.updateItem(job, JobUpdateSet.ERROR)
            throw e
        }
    }
}

