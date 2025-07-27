package net.kotlinx.domain.job.trigger

import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.batch.*
import net.kotlinx.domain.job.Job
import net.kotlinx.domain.job.JobExeDiv
import net.kotlinx.domain.job.JobStatus
import net.kotlinx.id.IdGenerator
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.koin.Koins

/** 배치. 각종 설정까지 들어감 */
class JobTriggerBatch(
    /** 이름 */
    override val name: String,
    /** 잡 def 이름 (vpc,메모리 등의 설정)  */
    val jobDefinitionName: String,
    /** 잡 큐 이름 (온디벤드/스팟 등의 설정)  */
    val jobQueueName: String,
    /** 배치 오버라이드  */
    val batchOverride: BatchOverride? = null,
) : JobTriggerMethod {

    private val log = KotlinLogging.logger {}
    override val jobExeDiv: JobExeDiv = JobExeDiv.BATCH

    private val jobSerializer: JobSerializer by Koins.koinLazy()
    private val idGenerator: IdGenerator by Koins.koinLazy()
    private val aws: AwsClient by Koins.koinLazy()

    override suspend fun trigger(op: JobTriggerOption): String {
        op.jobSk = op.jobSk ?: idGenerator.nextvalAsString()
        val jobParam = jobSerializer.toJson(op)
        if (op.preJobPersist) {
            jobSerializer.toJob(jobParam.toString().toGsonData())
        }

        if (op.jobStatus == JobStatus.RESERVED) {
            log.info { " => [$jobParam] 예약된 잡임으로 DDB 저장만 하고 실행은 스킵" }
            return jobParam.toString()
        }

        val findJob = Job(op.jobPk, op.jobSk!!)
        if (op.synch) {
            val jobDetail = aws.batch.submitJobAndWaitStarting(jobQueueName, jobDefinitionName, jobParam, batchOverride)
            log.debug { "잡 UI 링크 -> ${BatchUtil.toBatchUiLink(jobDetail.jobId!!)}" }
            log.debug { "잡 DDB 링크 -> ${findJob.dynamoItemLink}" }
        } else {
            val jobId = aws.batch.submitJob(jobQueueName, jobDefinitionName, jobParam, batchOverride)
            log.debug { "잡 UI 링크 -> ${BatchUtil.toBatchUiLink(jobId)}" }
            log.debug { "잡 DDB 링크 -> ${findJob.dynamoItemLink}" }
        }
        return jobParam.toString()
    }

}