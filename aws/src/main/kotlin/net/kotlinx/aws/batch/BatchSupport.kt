package net.kotlinx.aws.batch

import aws.sdk.kotlin.services.batch.BatchClient
import aws.sdk.kotlin.services.batch.describeJobs
import aws.sdk.kotlin.services.batch.model.JobDetail
import aws.sdk.kotlin.services.batch.model.JobStatus
import aws.sdk.kotlin.services.batch.model.SubmitJobResponse
import aws.sdk.kotlin.services.batch.submitJob
import mu.KotlinLogging
import net.kotlinx.core1.time.TimeFormat
import net.kotlinx.core2.concurrent.CoroutineSleepTool
import net.kotlinx.core2.gson.GsonData
import java.util.concurrent.TimeUnit.SECONDS

private val log = KotlinLogging.logger {}

/** 준비중인 상태인지 (로그스트림 아직 없음) */
fun JobStatus.isReady(): Boolean = this in setOf(JobStatus.Submitted, JobStatus.Pending, JobStatus.Runnable)

/** 단건 조회 */
suspend fun BatchClient.describeJob(jobId: String): JobDetail? = this.describeJobs { this.jobs = listOf(jobId) }.jobs!!.firstOrNull()

/**
 * 통합 간단 잡 제출 (코드 참조용)
 * @param jobQueueName 큐 이름
 * @param jobPk 등록된 잡 데피니션
 * @param json main args[] 의 첫 파라메터로 주입됨 -> cdk의 JobDefinitionProps.command 와 일치해야함. 편의상 json 하나로 관리
 */
suspend fun BatchClient.submitJob(jobQueueName: String, jobPk: String, json: GsonData): String {
    val resp: SubmitJobResponse = this.submitJob {
        this.jobQueue = jobQueueName
        this.jobName = "D${TimeFormat.DH_F01.get()}-${jobPk}" //단순히 콘솔에서 구분하기 위한 용도
        this.jobDefinition = jobPk
        this.parameters = mapOf(BatchUtil.MAIN_ARGS_KEY to json.toString())  //무조건 이 형식으로만 1뎁스로 입력해야함
    }
    return resp.jobId!!
}


/** 파게이트를 할당받을때까지 기다린다. (로그스트림 네임을 얻기 위함)  */
suspend fun BatchClient.submitJobAndWaitStarting(jobQueueName: String, jobPk: String, config: GsonData, limit: Int = 99): JobDetail {
    val jobId: String = this.submitJob(jobQueueName, jobPk, config)
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