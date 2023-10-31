package net.kotlinx.aws.batch

import aws.sdk.kotlin.services.batch.BatchClient
import aws.sdk.kotlin.services.batch.cancelJob
import aws.sdk.kotlin.services.batch.describeJobs
import aws.sdk.kotlin.services.batch.model.CancelJobResponse
import aws.sdk.kotlin.services.batch.model.JobDetail
import aws.sdk.kotlin.services.batch.model.JobStatus
import aws.sdk.kotlin.services.batch.model.SubmitJobResponse
import aws.sdk.kotlin.services.batch.submitJob
import mu.KotlinLogging
import net.kotlinx.core.concurrent.CoroutineSleepTool
import net.kotlinx.core.time.TimeFormat
import java.util.concurrent.TimeUnit.SECONDS

private val log = KotlinLogging.logger {}

/** 준비중인 상태인지 (로그스트림 아직 없음) */
fun JobStatus.isReady(): Boolean = this in setOf(JobStatus.Submitted, JobStatus.Pending, JobStatus.Runnable)

/** 단건 조회 */
suspend fun BatchClient.describeJob(jobId: String): JobDetail? = this.describeJobs { this.jobs = listOf(jobId) }.jobs!!.firstOrNull()

/** 취소 */
suspend fun BatchClient.describeJob(jobId: String, reason: String): CancelJobResponse = this.cancelJob {
    this.jobId = jobId
    this.reason = reason
}

/**
 * 통합 간단 잡 제출 (코드 참조용)
 * @param jobQueueName 큐 이름
 * @param jobPk 등록된 잡 데피니션
 * @param jobParam main args[] 의 첫 파라메터로 주입됨 -> cdk의 JobDefinitionProps.command 와 일치해야함. 편의상 json 하나로 관리
 */
suspend fun BatchClient.submitJob(jobQueueName: String, jobPk: String, jobParam: Any): String {
    val resp: SubmitJobResponse = this.submitJob {
        this.jobQueue = jobQueueName
        this.jobName = "D${TimeFormat.DH_F01.get()}-${jobPk}" //단순히 콘솔에서 구분하기 위한 용도
        this.jobDefinition = jobPk
        //배치는  args 로 단순 문자열만 받는다. 주의!! 무조건 1뎁스로 문자열로 입력해야함
        this.parameters = mapOf(
            BatchUtil.BATCH_ARGS01 to jobParam.toString(), //여기에 디폴트 입력
        )
    }
    return resp.jobId!!
}


/** 파게이트를 할당받을때까지 기다린다. (로그스트림 네임을 얻기 위함)  */
suspend fun BatchClient.submitJobAndWaitStarting(jobQueueName: String, jobPk: String, jobParam: Any, limit: Int = 99): JobDetail {
    val jobId: String = this.submitJob(jobQueueName, jobPk, jobParam)
    val sleepTool = CoroutineSleepTool(SECONDS.toMillis(1))
    for (i in 0..limit) {
        sleepTool.checkAndSleep()
        val jobDetail: JobDetail = this.describeJob(jobId) ?: throw IllegalStateException("job not found")
        log.debug { "  ==> 잡 [${jobPk}] : 상태 ${jobDetail.status}" }
        if (!jobDetail.status!!.isReady()) {
            return jobDetail
        }
    }
    throw IllegalStateException("너무 오래 대기했습니다.")
}