package net.kotlinx.domain.job.trigger

import com.google.common.eventbus.EventBus
import mu.KotlinLogging
import net.kotlinx.aws.AwsInstanceMetadata
import net.kotlinx.aws.AwsInstanceType
import net.kotlinx.domain.job.*
import net.kotlinx.domain.job.define.JobDefinitionRepository
import net.kotlinx.exception.toSimpleString
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.reflect.newInstance
import java.time.LocalDateTime

/**
 * 실제 실행서버의 로컬 머신 기준으로 job을 실행한다.
 * 여기에서는 개발 환경을 구분하지 않는다.
 */
class JobLocalExecutor(val profile: String? = null) {

    private val log = KotlinLogging.logger {}

    private val eventBus: EventBus by koinLazy()
    private val jobRepository by koinLazy<JobRepository>(profile)
    private val instanceMetadata by koinLazy<AwsInstanceMetadata>(profile)

    /**
     * @return pk 확인용 키 문자욜
     * */
    suspend fun runJob(job: Job): String {
        val jobDef = JobDefinitionRepository.findById(job.pk)
        val jobService = jobDef.jobClass.newInstance()
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

            //==============  싦행  ===================
            jobService.doRun(job)

            //==============  결과 마킹 ===================
            job.jobStatus = JobStatus.SUCCEEDED
            job.endTime = LocalDateTime.now()
            jobRepository.updateItem(job, JobUpdateSet.END)

            //실서버 강제호출의 경우 알람 전송
            if (job.instanceMetadata!!.instanceType == AwsInstanceType.BATCH) {
                if (job.jobExeFrom == JobExeFrom.ADMIN) {
                    eventBus.post(JobSuccessEvent(job))
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            log.error { "JOB 에러 => ${job.toKeyString()}" }
            run {
                job.jobStatus = JobStatus.FAILED
                job.jobContext = job.jobContext
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


}