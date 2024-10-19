package net.kotlinx.domain.job.trigger

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.batch.BatchUtil
import net.kotlinx.aws.batch.batch
import net.kotlinx.aws.batch.submitJob
import net.kotlinx.aws.batch.submitJobAndWaitStarting
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.aws.lambda.invokeAsynch
import net.kotlinx.aws.lambda.invokeSynch
import net.kotlinx.aws.lambda.lambda
import net.kotlinx.domain.job.Job
import net.kotlinx.domain.job.JobExeDiv
import net.kotlinx.domain.job.JobRepository
import net.kotlinx.domain.job.JobStatus
import net.kotlinx.id.IdGenerator
import net.kotlinx.json.gson.toGsonData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 개별 잡을 실제 실행시키는 로직
 * 새로은 실행 방법이 자유롭게 추가 가능해야한다.
 * 주의!! 설정 파일이지만, 의존 관계가 필요함으로 이미 의존성이 다 주입된 JobTrigger를 역참조 한다.
 *  */
interface JobTriggerMethod : KoinComponent {

    /** 이름 */
    val name: String

    /** 실행 구분 ex) 람다.. */
    val jobExeDiv: JobExeDiv

    /**
     * 실제 트리거
     * @param op 실제 트리거 옵션
     *  */
    suspend fun trigger(op: JobTriggerOption): String

    /** 개발자 PC 로컬 환경을 의미한다 */
    object LOCAL : JobTriggerMethod {
        override val name: String = "local"
        override val jobExeDiv: JobExeDiv = JobExeDiv.LOCAL

        val jobSerializer: JobSerializer by inject()
        val jobLocalExecutor: JobLocalExecutor by inject()

        override suspend fun trigger(op: JobTriggerOption): String {
            val input = jobSerializer.toJson(op)
            val job = jobSerializer.toJob(input.toString().toGsonData())!!

            if (job.jobStatus == JobStatus.RESERVED) return job.toKeyString()

            return if (op.synch) {
                jobLocalExecutor.runJob(job)
            } else {
                //비동기는 그냥 스래드 생성해서 실행 (스래드풀X)
                Thread {
                    runBlocking {
                        jobLocalExecutor.runJob(job)
                    }
                }.start()
                job.toKeyString()
            }
        }
    }
}

/** 람다 */
class JobTriggerLambda(
    /** 이름 */
    override val name: String,
    /** 호출용 람다 함수 이름 */
    val lambdaFunctionName: String,
) : JobTriggerMethod {

    private val log = KotlinLogging.logger {}

    override val jobExeDiv: JobExeDiv = JobExeDiv.LAMBDA

    private val jobSerializer: JobSerializer by inject()
    private val idGenerator: IdGenerator by inject()
    private val aws1: AwsClient by inject()
    private val jobRepository: JobRepository by inject()

    override suspend fun trigger(op: JobTriggerOption): String {
        val jobSk = op.jobSk ?: idGenerator.nextvalAsString()
        val jobParam = jobSerializer.toJson(op, jobSk)
        if (op.synch) {
            val resultText = aws1.lambda.invokeSynch(lambdaFunctionName, jobParam)
            log.info { "람다 실행 [$jobParam] - 동기화(${op.synch}) -> 결과 (${resultText.ok}) ->  결과문자열 ${resultText.result}" }
        } else {
            aws1.lambda.invokeAsynch(lambdaFunctionName, jobParam)
            log.info { "람다 실행 [$jobParam] - 동기화(${op.synch})" }
        }
        if (log.isDebugEnabled) {
            val findJob = Job(op.jobPk, jobSk)
            if (op.synch) {
                jobRepository.getItem(findJob)?.let {
                    log.debug { "로그링크 : ${it.toLogLink()}" }
                }
            }
        }
        return jobParam.toString()
    }

}

/** 배치. 각종 설정까지 들어감 */
class JobTriggerBatch(
    /** 이름 */
    override val name: String,
    /** 잡 def 이름 (vpc,메모리 등의 설정)  */
    val jobDefinitionName: String,
    /** 잡 큐 이름 (온디벤드/스팟 등의 설정)  */
    val jobQueueName: String,
) : JobTriggerMethod {

    private val log = KotlinLogging.logger {}
    override val jobExeDiv: JobExeDiv = JobExeDiv.BATCH

    private val jobSerializer: JobSerializer by inject()
    private val idGenerator: IdGenerator by inject()
    private val aws1: AwsClient by inject()

    override suspend fun trigger(op: JobTriggerOption): String {
        val jobSk = op.jobSk ?: idGenerator.nextvalAsString()
        val jobParam = jobSerializer.toJson(op, jobSk)
        val findJob = Job(op.jobPk, jobSk)
        if (op.synch) {
            val jobDetail = aws1.batch.submitJobAndWaitStarting(jobQueueName, jobDefinitionName, jobParam)
            log.debug { "잡 UI 링크 -> ${BatchUtil.toBatchUiLink(jobDetail.jobId!!)}" }
            log.debug { "잡 DDB 링크 -> ${findJob.toConsoleLink()}" }
        } else {
            val jobId = aws1.batch.submitJob(jobQueueName, jobDefinitionName, jobParam)
            log.debug { "잡 UI 링크 -> ${BatchUtil.toBatchUiLink(jobId)}" }
            log.debug { "잡 DDB 링크 -> ${DynamoUtil.toConsoleLink(findJob.tableName, findJob)}" }
        }
        return jobParam.toString()
    }

}