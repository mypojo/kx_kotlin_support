package net.kotlinx.module.job.trigger

import com.lectra.koson.ObjectType
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.id.IdGenerator
import net.kotlinx.core.string.enumValueOf
import net.kotlinx.module.job.*
import net.kotlinx.module.job.define.JobDefinitionUtil
import net.kotlinx.module.job.define.JobExecuteType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * 원격으로 잡을 실행하기 위한 잡 설정(json) <-> job 객체 변환
 */
class JobSerializer : KoinComponent {

    private val log = KotlinLogging.logger {}

    private val idGenerator: IdGenerator by inject()
    private val jobRepository: JobRepository by inject()

    /**
     * 필요시 오버라이드
     * null 이면 job 처리 요청이 아님
     *  */
    suspend fun toJob(input: GsonData, block: Job.() -> Unit = {}): Job? {
        val jobPk = input[AwsNaming.JOB_PK].str ?: return null
        val jobSk = input[AwsNaming.JOB_SK].str ?: idGenerator.nextvalAsString()

        val job = Job(jobPk, jobSk) {
            val jobDefinition = JobDefinitionUtil.findById(pk)
            val jobTrigger = jobDefinition.jobTriggerMethod
            reqTime = LocalDateTime.now()
            jobStatus = JobStatus.STARTING
            jobEnv = jobTrigger.name
            ttl = when (jobTrigger.jobExeDiv) {
                JobExeDiv.LOCAL -> DynamoUtil.ttlFromNow(TimeUnit.HOURS, 1)  //로컬은 테스트로 간주하고 1시간 보관
                else -> DynamoUtil.ttlFromNow(TimeUnit.DAYS, 7 * 2)
            }

            //파싱값 입력 4개
            jobOption = input[Job::jobOption.name].str
            input[Job::memberId.name].str?.let { memberId = it }
            jobExeFrom = enumValueOf(input[Job::jobExeFrom.name].str, JobExeFrom.UNKNOWN)
            sfnId = input[Job::sfnId.name].str

            //특수한 경우 예외 처리해줌
            if (jobDefinition.jobExecuteType == JobExecuteType.LAMBDA_SYNCH_NOLOG) {
                persist = false
            }

            log.debug { " -> job 신규생성 $pk / $sk" }
        }
        block(job)
        jobRepository.putItem(job)
        return job
    }

    /** job을 AWS lambda / BATCH 에 전송할 잡 설정(DDB 키값전송)으로 변환 */
    fun toJson(op: JobTriggerOption, jobSk: String? = null): ObjectType = obj {
        AwsNaming.JOB_PK to op.jobDefinition.jobPk
        jobSk?.let { AwsNaming.JOB_SK to it }  //sk는 옵션이다.

        //파싱값 입력 4개
        Job::jobOption.name to op.jobOption.toString() //이건 무조건 문자열로 입력
        Job::memberId.name to op.memberId
        Job::jobExeFrom.name to op.jobExeFrom.name
        Job::sfnId.name to op.sfnId
    }

}