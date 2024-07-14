package net.kotlinx.domain.job.trigger

import com.lectra.koson.ObjectType
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.aws.AwsNaming
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.domain.job.*
import net.kotlinx.domain.job.define.JobDefinitionRepository
import net.kotlinx.domain.job.define.JobExecuteType
import net.kotlinx.id.IdGenerator
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.string.enumValueOf
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * 원격으로 잡을 실행하기 위한 잡 설정(json) <-> job 객체 변환
 */
class JobSerializer(val profile: String? = null) {

    private val log = KotlinLogging.logger {}

    private val idGenerator by koinLazy<IdGenerator>()
    private val jobRepository by koinLazy<JobRepository>(profile)

    /**
     * 필요시 오버라이드
     * null 이면 job 처리 요청이 아님
     *  */
    suspend fun toJob(input: GsonData, block: Job.() -> Unit = {}): Job? {
        val jobPk = input[AwsNaming.JOB_PK].str ?: return null
        val jobSk = input[AwsNaming.JOB_SK].str ?: idGenerator.nextvalAsString()

        val job = Job(jobPk, jobSk) {
            val jobDefinition = JobDefinitionRepository.findById(pk)
            val jobTrigger = jobDefinition.jobTriggerMethod
            reqTime = LocalDateTime.now()
            jobStatus = JobStatus.STARTING
            jobEnv = jobTrigger.name
            ttl = when (jobTrigger.jobExeDiv) {
                JobExeDiv.LOCAL -> DynamoUtil.ttlFromNow(TimeUnit.HOURS, 1)  //로컬은 테스트로 간주하고 1시간 보관
                else -> DynamoUtil.ttlFromNow(TimeUnit.DAYS, 7 * 2)
            }

            //파싱값 입력 4개
            jobOption = input[Job::jobOption.name].str!!.toGsonData()
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