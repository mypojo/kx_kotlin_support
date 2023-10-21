package net.kotlinx.module.job.trigger

import aws.sdk.kotlin.services.sfn.model.ExecutionStatus
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.kotlinx.aws.batch.BatchUtil
import net.kotlinx.aws.batch.submitJob
import net.kotlinx.aws.batch.submitJobAndWaitStarting
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.aws.lambda.invokeAsynch
import net.kotlinx.aws.lambda.invokeSynch
import net.kotlinx.aws.sfn.listExecutions
import net.kotlinx.aws.sfn.startExecution
import net.kotlinx.core.exception.KnownException
import net.kotlinx.module.job.Job
import net.kotlinx.module.job.JobExeDiv
import kotlin.time.Duration.Companion.seconds

/**
 * 개별 잡을 실제 실행시키는 로직
 * 새로은 실행 방법이 자유롭게 추가 가능해야한다.
 * 주의!! 설정 파일이지만, 의존 관계가 필요함으로 이미 의존성이 다 주입된 JobTrigger를 역참조 한다.
 *  */
interface JobTriggerMethod {

    /** 이름 */
    val name: String

    /** 실행 구분 ex) 람다.. */
    val jobExeDiv: JobExeDiv

    /** 실제 트리거 */
    suspend fun trigger(op: JobTriggerOption, jobTrigger: JobTrigger, job: Job)

    object NON : JobTriggerMethod {
        override val name: String = "unknown"
        override val jobExeDiv: JobExeDiv = JobExeDiv.LOCAL
        override suspend fun trigger(op: JobTriggerOption, jobTrigger: JobTrigger, job: Job) = throw UnsupportedOperationException()
    }

    object LOCAL : JobTriggerMethod {
        override val name: String = "local"
        override val jobExeDiv: JobExeDiv = JobExeDiv.LOCAL
        override suspend fun trigger(op: JobTriggerOption, jobTrigger: JobTrigger, job: Job) {
            jobTrigger.jobLocalExecutor.runJob(job)
        }
    }

}

/** step function */
class JobTriggerSfn(
    /** 이름 */
    override val name: String,
    val machinesNameBuilder: (jobPk: String) -> String,
) : JobTriggerMethod {

    private val log = KotlinLogging.logger {}

    override val jobExeDiv: JobExeDiv = JobExeDiv.STEP_FUNCTIONS

    override suspend fun trigger(op: JobTriggerOption, jobTrigger: JobTrigger, job: Job) {
        val machinesName: String = machinesNameBuilder(job.pk)
        //중복실행 막음
        val awsId = jobTrigger.aws.awsConfig.awsId!!
        jobTrigger.aws.sfn.listExecutions(awsId, machinesName, ExecutionStatus.Running).also {
            val executions = it.executions!!
            log.debug { " -> 지금 실행중인 작업 : ${executions.size}" }
            if (executions.isEmpty()) return@also

            throw KnownException.ItemRetryException("[${machinesName}] 이미 작동중인 작업 ${executions.size}건이 존재합니다. 샘플링크 : ${executions.first().executionArn}")
        }
        val execution = jobTrigger.aws.sfn.startExecution(awsId, machinesName, op.jobOption)
        log.info { "sfn 실행 : ${execution.executionArn}" }
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

    override suspend fun trigger(op: JobTriggerOption, jobTrigger: JobTrigger, job: Job) {
        val param = jobTrigger.jobSerializer.toJson(job)
        if (op.synch) {
            val resultText = jobTrigger.aws.lambda.invokeSynch(lambdaFunctionName, param)
            log.debug { "람다 실행 - 동기화(${op.synch}) -> 결과 (${resultText.ok}) ->  결과문자열 ${resultText.result}" }
        } else {
            jobTrigger.aws.lambda.invokeAsynch(lambdaFunctionName, param)
        }
        if (log.isDebugEnabled) {
            log.debug { "잡 DDB 링크 -> ${job.toConsoleLink()}" }
            if (op.synch) {
                jobTrigger.jobRepository.getItem(job)?.let {
                    log.debug { "로그링크 : ${it.toLogLink()}" }
                }
            }
        }
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
    override suspend fun trigger(op: JobTriggerOption, jobTrigger: JobTrigger, job: Job) {

        val param = jobTrigger.jobSerializer.toJson(job)
        if (op.synch) {
            val jobDetail = jobTrigger.aws.batch.submitJobAndWaitStarting(jobQueueName, jobDefinitionName, param)
            log.debug { "잡 UI 링크 -> ${BatchUtil.toBatchUiLink(jobDetail.jobId!!)}" }
            log.debug { "잡 DDB 링크 -> ${job.toConsoleLink()}" }
            while (true) {
                delay(10.seconds.inWholeMilliseconds) //STARTING 에서 실제 도커 인스턴스가 올라가는데까지 시간이 걸림
                val current = jobTrigger.jobRepository.getItem(job) ?: throw IllegalStateException("없을리가?")
                if (current.jobStatus.readyToRun()) {
                    log.debug { " -> 아직 준비중... ${current.jobStatus}" }
                    continue
                }
                log.debug { "잡 준비완료. ${current.jobStatus}" }
                current.awsInfo?.let {
                    log.debug { "로그링크 : ${current.toLogLink()}" }
                }
                break
            }
        } else {
            val jobId = jobTrigger.aws.batch.submitJob(jobQueueName, jobDefinitionName, param)
            log.debug { "잡 UI 링크 -> ${BatchUtil.toBatchUiLink(jobId)}" }
            log.debug { "잡 DDB 링크 -> ${DynamoUtil.toConsoleLink(job.tableName, job)}" }
        }

    }

}