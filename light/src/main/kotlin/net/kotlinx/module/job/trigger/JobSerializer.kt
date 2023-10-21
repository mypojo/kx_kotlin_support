package net.kotlinx.module.job.trigger

import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.kotlinx.aws.batch.BatchUtil
import net.kotlinx.core.gson.GsonData
import net.kotlinx.module.job.Job
import net.kotlinx.module.job.JobExeFrom
import net.kotlinx.module.job.JobFactory
import net.kotlinx.module.job.JobRepository
import kotlin.time.Duration.Companion.seconds

/**
 * 원격으로 잡을 실행하기 위한 잡 설정(json) <-> job 객체 변환
 */
class JobSerializer(
    private val jobRepository: JobRepository,
    private val jobFactory: JobFactory,
) {

    private val log = KotlinLogging.logger {}

    /** 필요시 오버라이드 */
    suspend fun toJob(json: GsonData): Job? {
        val jobPk = json[JOB_PK_KEY].str ?: return null
        val jobSk = json[JOB_SK_KEY].str

        return when {
            jobSk == null -> {
                log.info { " -> job($jobPk) 신규 생성(sfn) 됩니다." }
                val job = jobFactory.create(jobPk).apply {
                    jobExeFrom = JobExeFrom.SFN
                    val inputJobOption = GsonData.parse(json[Job::jobOption.name].str)
                    inputJobOption.remove(Job::sfnId.name)?.let {
                        sfnId = it.str
                    }
                    jobOption = inputJobOption.toString()

                }
                jobRepository.putItem(job)
                job
            }

            else -> {
                var job = jobRepository.getItem(Job(jobPk, jobSk))
                if (job == null) {
                    log.warn { "1초후 다시 DDB 접근" }
                    delay(1.seconds)
                    job = jobRepository.getItem(Job(jobPk, jobSk)) //입력 전에 get 할 수 있는듯.. 지속되면 수정
                }
                if (job == null) {
                    log.warn { "5초후 다시 DDB 접근" }
                    delay(5.seconds)
                    job = jobRepository.getItem(Job(jobPk, jobSk)) //입력 전에 get 할 수 있는듯.. 지속되면 수정
                }
                checkNotNull(job) { "DDB job $jobPk $jobSk  => not found" }
                check(job.jobStatus.readyToRun()) { "job status ${job.jobStatus} not required" }
                job
            }
        }
    }

    /** job을 AWS lambda / BATCH 에 전송할 잡 설정(DDB 키값전송)으로 변환 */
    fun toJson(job: Job): GsonData = GsonData.obj().apply {
        put(JOB_PK_KEY, job.pk)
        put(JOB_SK_KEY, job.sk)
    }


    /** 상수를 private 으로 관리한다. */
    companion object {

        //==================================================== 일반 ======================================================
        /** GSON KEY : job pk. 필수 입력항목임.  = jobDiv  */
        private const val JOB_PK_KEY = BatchUtil.JOB_PK

        /** GSON KEY : job sk. 전달 항목에 이게 없으면 새로 생성함.  */
        private const val JOB_SK_KEY = BatchUtil.JOB_SK

    }


}