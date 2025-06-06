package net.kotlinx.domain.job.trigger

import com.google.common.eventbus.EventBus
import mu.KotlinLogging
import net.kotlinx.aws.AwsInstanceMetadata
import net.kotlinx.domain.job.*
import net.kotlinx.domain.job.define.JobDefinitionRepository
import net.kotlinx.exception.KnownException
import net.kotlinx.exception.toSimpleString
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.reflect.newInstance
import java.time.LocalDateTime

/**
 * 실제 실행서버의 로컬 머신 기준으로 job을 실행한다.
 * 여기에서는 개발 환경을 구분하지 않는다.
 */
class JobLocalExecutor() {

    private val log = KotlinLogging.logger {}

    private val eventBus: EventBus by koinLazy()
    private val jobRepository by koinLazy<JobRepository>()
    private val instanceMetadata by koinLazy<AwsInstanceMetadata>()

    /**
     * @return pk 확인용 키 문자욜
     * */
    suspend fun execute(job: Job): String {
        val jobDef = JobDefinitionRepository.findById(job.pk)
        val jobTasklet = jobDef.jobClass.newInstance()
        job.instanceMetadata = instanceMetadata

        log.debug { "job run (${job.toKeyString()})" }
        job.jobOption?.let {
            log.debug { " -> job option : ${job.jobOption}" }
        }

        try {

            JobHolder.JOB.set(job)

            //==============  RUNNING 마킹 ===================
            job.jobStatus = JobStatus.RUNNING
            job.startTime = LocalDateTime.now()
            jobRepository.updateItem(job, JobUpdateSet.START)

            //==============  실행  ===================
            jobTasklet.execute(job)

            //==============  결과 마킹 ===================
            when (job.jobStatus) {
                JobStatus.RUNNING -> {
                    job.jobStatus = JobStatus.SUCCEEDED
                    job.endTime = LocalDateTime.now()
                    jobRepository.updateItem(job, JobUpdateSet.END)

                    eventBus.post(JobSuccessEvent(job))
                }

                JobStatus.PROCESSING -> {
                    jobRepository.updateItem(job, JobUpdateSet.STATUS)
                }

                else -> {
                    throw IllegalStateException("JobStatus is ${job.jobStatus} but not RUNNING or PROCESSING")
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            log.error { "JOB 에러 => ${job.toKeyString()}" }
            run {
                job.jobStatus = JobStatus.FAILED
                job.jobErrMsg = e.toSimpleString()
                job.endTime = LocalDateTime.now()
                jobRepository.updateItem(job, JobUpdateSet.ERROR)
            }
            eventBus.post(JobFailEvent(job, e))
            throw JobException(e) //예외를 반드시 던져야 한다.
        } finally {
            JobHolder.JOB.remove()
        }

        return job.toKeyString()
    }


    /**
     * 외부 이벤트 등으로 잡을 완료시킴
     * @param block job에 대한 전처리
     *  */
    suspend fun resume(jobPk: String, jobSk: String, block: suspend (Job) -> Unit = {}) {
        val job = jobRepository.getItem(Job(jobPk, jobSk))!!
        block(job)
        val jobService = JobDefinitionRepository.findById(job.pk).jobClass.newInstance()
        try {
            jobService.onProcessComplete(job)
            this.resumeSuccess(job)
        } catch (e: KnownException.ItemSkipException) {
            log.warn { "onProcessComplete 처리중 스킵! -> ${e.toSimpleString()}" }
        } catch (e: Exception) {
            log.warn { "onProcessComplete 처리중 예외! -> ${e.toSimpleString()}" }
            e.printStackTrace()
            this.resumeFail(job, "onProcessComplete 처리중 예외! -> ${e.toSimpleString()}")
        }

    }


    //==================================================== 콜백 ======================================================

    /** resume 내 성공처리 */
    suspend fun resumeSuccess(job: Job): String {
        job.jobStatus = JobStatus.SUCCEEDED
        job.endTime = LocalDateTime.now()
        jobRepository.updateItem(job, JobUpdateSet.END)

        eventBus.post(JobSuccessEvent(job))
        return job.toKeyString()
    }

    suspend fun resumeFail(jobPk: String, jobSk: String, jobErrMsg: String, block: suspend (Job) -> Unit = {}): String {
        val job = jobRepository.getItem(Job(jobPk, jobSk))!!
        block(job)
        return resumeFail(job, jobErrMsg)
    }

    /** resume 내 실패처리 */
    suspend fun resumeFail(job: Job, jobErrMsg: String): String {
        job.jobStatus = JobStatus.FAILED
        job.endTime = LocalDateTime.now()
        job.jobErrMsg = jobErrMsg
        jobRepository.updateItem(job, JobUpdateSet.ERROR)

        eventBus.post(JobFailEvent(job, RuntimeException(jobErrMsg)))
        return job.toKeyString()
    }


}