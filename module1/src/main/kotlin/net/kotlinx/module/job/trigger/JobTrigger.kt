package net.kotlinx.module.job.trigger

import aws.sdk.kotlin.services.sfn.model.ExecutionStatus
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.batch.BatchUtil
import net.kotlinx.aws.batch.submitJob
import net.kotlinx.aws.batch.submitJobAndWaitStarting
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.aws.lambda.invokeAsynch
import net.kotlinx.aws.lambda.invokeSynch
import net.kotlinx.aws.sfn.listExecutions
import net.kotlinx.aws.sfn.startExecution
import net.kotlinx.core.exception.KnownException.ItemRetryException
import net.kotlinx.core.lib.toSimpleString
import net.kotlinx.module.job.Job
import net.kotlinx.module.job.JobExeDiv
import net.kotlinx.module.job.JobFactory
import net.kotlinx.module.job.JobRepository
import net.kotlinx.module.job.JobStatus.FAILED
import net.kotlinx.module.job.run.JobUpdateSet
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

private val log = KotlinLogging.logger {}

interface JobTrigger {
    /** 이름 */
    val name: String
    val jobExeDiv: JobExeDiv
    suspend fun trigger(op: JobTriggerOption)

    object NON : JobTrigger {
        override val name: String = "unknown"
        override val jobExeDiv: JobExeDiv = JobExeDiv.local
        override suspend fun trigger(op: JobTriggerOption) = throw UnsupportedOperationException()
    }
}

/**
 * 잡 트리거 환경 상세 정의. 미리 정의되어서 잡 정의에 포함됨
 * ex) lambda & 메소드명
 * ex) batch & vcpu 수
 */
abstract class AbstractJobTrigger : JobTrigger, KoinComponent {

    //==================================================== 주입 ======================================================
    protected val jobRepository: JobRepository by inject()
    protected val jobFactory: JobFactory by inject()
    protected val jobSerializer: JobSerializer by inject()
    protected val aws: AwsClient by inject()

    abstract suspend fun doTrigger(job: Job, op: JobTriggerOption)

    override suspend fun trigger(op: JobTriggerOption) {
        val job: Job = jobFactory.create(op.jobDefinition.jobPk)
        job.jobExeFrom = op.jobExeFrom
        job.jobOption = op.jobOption.toString()
        try {
            doTrigger(job, op)
        } catch (e: Throwable) {
            log.warn { "잡 트리거중 예외!!" }
            job.jobStatus = FAILED
            job.jobErrMsg = e.toSimpleString()
            job.endTime = LocalDateTime.now()
            jobRepository.updateItem(job, JobUpdateSet.ERROR)
            throw e
        }
    }
}

/** 로컬 */
open abstract class JobLocal : AbstractJobTrigger() {
    override val name: String = "local"
    override val jobExeDiv: JobExeDiv = JobExeDiv.local
}

/** step function */
open class JobSfn(
    /** 이름 */
    override val name: String,
    val machinesNameBuilder: (jobPk: String) -> String,
) : AbstractJobTrigger() {
    override val jobExeDiv: JobExeDiv = JobExeDiv.lambda
    override suspend fun doTrigger(job: Job, op: JobTriggerOption) {
        val machinesName: String = machinesNameBuilder(job.pk)
        //중복실행 막음
        val awsId = aws.awsConfig.awsId!!
        aws.sfn.listExecutions(awsId, machinesName, ExecutionStatus.Running).also {
            val executions = it.executions!!
            log.debug { " -> 지금 실행중인 작업 : ${executions.size}" }
            if (executions.isEmpty()) return@also

            throw ItemRetryException("[${machinesName}] 이미 작동중인 작업 ${executions.size}건이 존재합니다. 샘플링크 : ${executions.first().executionArn}")
        }
        val execution = aws.sfn.startExecution(awsId, machinesName, op.jobOption)
        log.info { "sfn 실행 : ${execution.executionArn}" }
    }
}

/** 람다 */
open class JobLambda(
    /** 이름 */
    override val name: String,
    /** 호출용 람다 함수 이름 */
    val lambdaFunctionName: String,
) : AbstractJobTrigger() {
    override val jobExeDiv: JobExeDiv = JobExeDiv.lambda
    override suspend fun doTrigger(job: Job, op: JobTriggerOption) {
        jobRepository.putItem(job)
        val param = jobSerializer.toJson(job)
        if (op.synch) {
            val resultText = aws.lambda.invokeSynch(lambdaFunctionName, param)
            log.debug { "람다 실행 - 동기화(${op.synch}) -> 결과 (${resultText.ok}) ->  결과문자열 ${resultText.result}" }
        } else {
            aws.lambda.invokeAsynch(lambdaFunctionName, param)
        }
        if (log.isDebugEnabled) {
            log.debug { "잡 DDB 링크 -> ${job.toConsoleLink()}" }
            if (op.synch) {
                jobRepository.getItem(job)?.let {
                    log.debug { "로그링크 : ${it.toLogLink()}" }
                }
            }
        }
    }
}

/** 배치. 각종 설정까지 들어감 */
open class JobBatch(
    /** 이름 */
    override val name: String,
    /** 잡 def 이름 (vpc,메모리 등의 설정)  */
    val jobDefinitionName: String,
    /** 잡 큐 이름 (온디벤드/스팟 등의 설정)  */
    val jobQueueName: String,
) : AbstractJobTrigger() {
    override val jobExeDiv: JobExeDiv = JobExeDiv.batch
    override suspend fun doTrigger(job: Job, op: JobTriggerOption) {
        jobRepository.putItem(job)
        val param = jobSerializer.toJson(job)
        if (op.synch) {
            val jobDetail = aws.batch.submitJobAndWaitStarting(jobQueueName, jobDefinitionName, param)
            log.debug { "잡 UI 링크 -> ${BatchUtil.toBatchUiLink(jobDetail.jobId!!)}" }
            log.debug { "잡 DDB 링크 -> ${job.toConsoleLink()}" }
            while (true) {
                delay(10.seconds.inWholeMilliseconds) //STARTING 에서 실제 도커 인스턴스가 올라가는데까지 시간이 걸림
                val current = jobRepository.getItem(job) ?: throw IllegalStateException("없을리가?")
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
            val jobId = aws.batch.submitJob(jobQueueName, jobDefinitionName, param)
            log.debug { "잡 UI 링크 -> ${BatchUtil.toBatchUiLink(jobId)}" }
            log.debug { "잡 DDB 링크 -> ${DynamoUtil.toConsoleLink(job.tableName, job)}" }
        }
    }
}

///** 스탭펑션 예약 샘플 */
//open class JobSfnReserve(
//    /** AWS ID  */
//    val awsId: String,
//    /** 이름 */
//    override val name: String,
//    /** 잡 def 이름  */
//    val stateMachineName: String,
//    /** 예약 sfn에 등록된 잡 트리거 프리픽스 이름.  reserveSfnPrefix + jobExeDiv.name  이렇게 CDK에 등록되면 됨 */
//    val reserveSfnPrefix: String = "JobTrigger_",
//
//    ) : AbstractJobTrigger() {
//    override val jobExeDiv: JobExeDiv = stepFunctions
//    override suspend fun doTrigger(job: Job, op: JobTriggerOption) {
//        job.jobStatus = SCHEDULED
//        jobRepository.putItem(job)
//        val param = jobSerializer.toJson(job)
//
//        //기본 토큰에 모든 정보를 넣는다. 이를 어떻게 읽는지는 CDK 참조
//        val scheduledTime = job.jobScheduleTime!!
//        //이상하다.. 수정할것
//        val context = GsonData.obj().apply {
//            put(SCHEDULE_TIME, scheduledTime.toIos())
//            put(reserveSfnPrefix + job.pk, param.toString()) //job 의 경우 네임의 설정을 args[] 첫 인자로 전달한다. & 단순 문자열이여야함.
//        }
//        //aws.sfn.startExecution(awsId, stateMachineName, context.toString(), job.sfnId!!)
//        log.debug("잡 [{}] 예약 -> {} 에 실행됩니다.", job.pk, TimeFormat.YMDHMS_K01[scheduledTime])
//    }
//
//    companion object {
//
//        //==================================================== 스케쥴 ======================================================
//        /** GSON KEY : 예약시간(존포함) 키값  */
//        private const val SCHEDULE_TIME = "jobScheduleTime"
//
//        /** GSON KEY : 실행 방법  */
//        private const val EXE_DIV = "jobExeDiv"
//    }
//}

