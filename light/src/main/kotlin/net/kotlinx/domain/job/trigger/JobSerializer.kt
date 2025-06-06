package net.kotlinx.domain.job.trigger

import com.lectra.koson.ObjectType
import com.lectra.koson.obj
import mu.KotlinLogging
import net.kotlinx.aws.AwsNaming
import net.kotlinx.domain.job.Job
import net.kotlinx.domain.job.JobExeFrom
import net.kotlinx.domain.job.JobRepository
import net.kotlinx.domain.job.JobStatus
import net.kotlinx.domain.job.define.JobDefinitionRepository
import net.kotlinx.domain.job.define.JobExecuteType
import net.kotlinx.id.IdGenerator
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.koin.Koins.koinLazy
import java.time.LocalDateTime

/**
 * 원격으로 잡을 실행하기 위한 잡 설정(json) <-> job 객체 변환
 */
class JobSerializer() {

    private val log = KotlinLogging.logger {}

    private val idGenerator by koinLazy<IdGenerator>()
    private val jobRepository by koinLazy<JobRepository>()

    /**
     * 필요시 오버라이드
     * null 이면 job 처리 요청이 아님
     *  */
    suspend fun toJob(input: GsonData, block: Job.() -> Unit = {}): Job? {
        val jobPk = input[AwsNaming.JOB_PK].str ?: return null
        val jobSk = input[AwsNaming.JOB_SK].str ?: idGenerator.nextvalAsString()

        //#1 디폴트로 기존 DDB 데이터 사용 (재시도시 lastSfnId 읽어오기 등등)
        //#2 입력된 JSON 으로 오버라이드
        val job = Job(jobPk, jobSk).let {
            jobRepository.getItem(it) ?: it
        }.apply {
            val jobDefinition = JobDefinitionRepository.findById(pk)
            val jobTrigger = jobDefinition.jobTriggerMethod

            //무조건 오버라이드 하는값
            reqTime = LocalDateTime.now()
            jobEnv = jobTrigger.name
            jobExeFrom = input.enum<JobExeFrom>(Job::jobExeFrom.name) ?: JobExeFrom.UNKNOWN
            jobStatus = input.enum<JobStatus>(Job::jobStatus.name) ?: JobStatus.STARTING

            //옵션에 있으면 오버라이드 하는값 (아래  toJson() 하고 1:1 매핑)
            input[Job::jobOption.name].str?.let { jobOption = it.toGsonData() }
            input[Job::memberId.name].str?.let { memberId = it }

            //특수한 경우 예외 처리해줌
            if (jobDefinition.jobExecuteType == JobExecuteType.NOLOG) {
                persist = false
            }

            log.debug { " -> job 신규생성 $pk / $sk" }
        }
        block(job)
        jobRepository.putItem(job) //이경우 여기서 오버라이드 쓰기 -> STARTING 으로 표기됨
        return job
    }

    /**
     * job을 AWS lambda / BATCH 에 전송할 잡 설정(DDB 키값전송)으로 변환
     * 전체를 그냥 시리얼라이즈 하기에는.. 불안 요소가 있어서 선택적으로 작업함
     *  */
    fun toJson(op: JobTriggerOption): ObjectType = obj {
        AwsNaming.JOB_PK to op.jobPk
        op.jobSk?.let { AwsNaming.JOB_SK to it }  //sk는 옵션이다.

        //파싱값 입력 4개
        Job::jobOption.name to op.jobOption.toString() //이건 무조건 문자열로 입력
        Job::memberId.name to op.memberId

        Job::jobStatus.name to op.jobStatus
        Job::jobExeFrom.name to op.jobExeFrom.name
    }

}