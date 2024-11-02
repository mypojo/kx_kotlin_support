package net.kotlinx.domain.job.trigger

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.AwsInstanceType
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.batch.BatchUtil
import net.kotlinx.aws.batch.batch
import net.kotlinx.aws.batch.submitJob
import net.kotlinx.aws.batch.submitJobAndWaitStarting
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.toErrorLogLink
import net.kotlinx.aws.lambda.invokeAsynch
import net.kotlinx.aws.lambda.invokeSynch
import net.kotlinx.aws.lambda.lambda
import net.kotlinx.domain.job.Job
import net.kotlinx.domain.job.JobExeDiv
import net.kotlinx.domain.job.JobRepository
import net.kotlinx.domain.job.JobStatus
import net.kotlinx.id.IdGenerator
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.koin.Koins.koinLazy

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

    /**
     * 실제 트리거
     * @param op 실제 트리거 옵션
     *  */
    suspend fun trigger(op: JobTriggerOption): String

    /**
     * 아래의 환경일 수 있음
     * 개발자 PC 로컬
     * 웹서버
     * 람다 호출
     *  */
    object LOCAL : JobTriggerMethod {
        override val name: String = "local"
        override val jobExeDiv: JobExeDiv = JobExeDiv.LOCAL

        private val jobSerializer: JobSerializer by koinLazy()
        private val jobLocalExecutor: JobLocalExecutor by koinLazy()

        private val log = KotlinLogging.logger {}

        override suspend fun trigger(op: JobTriggerOption): String {
            val input = jobSerializer.toJson(op)
            log.trace { "실제 DDB에는 여기서 최종 입력됨" }
            val job = jobSerializer.toJob(input.toString().toGsonData())!!

            if (job.jobStatus == JobStatus.RESERVED) return job.toKeyString()

            return when (AwsInstanceTypeUtil.INSTANCE_TYPE) {

                /**
                 * 람다는 메인스래드를 중지하지 않은 상태에서 백그라운드 스래드 작동이 불가능하다
                 * ex) 이벤트 스케쥴러에서 트리거된 작업을 그대로 실행하는 경우
                 * */
                AwsInstanceType.LAMBDA -> {
                    log.debug { "JobTriggerMethod LOCAL (${job.toKeyString()}) -> 동기화(${AwsInstanceTypeUtil.INSTANCE_TYPE}) 실행" }
                    jobLocalExecutor.execute(job)
                }

                else -> {
                    if (op.synch) {
                        log.debug { "JobTriggerMethod LOCAL (${job.toKeyString()}) -> 동기화 실행" }
                        jobLocalExecutor.execute(job)
                    } else {
                        //비동기는 그냥 스래드 생성해서 실행 (스래드풀X)
                        //주의!! 람다의 경우 이렇게 하면 호출이 시작만 스킵될 수 있음
                        log.debug { "JobTriggerMethod LOCAL (${job.toKeyString()}) -> 비동기화 실행" }
                        Thread {
                            runBlocking {
                                jobLocalExecutor.execute(job)
                            }
                        }.start()
                        job.toKeyString()
                    }
                }
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

    private val jobSerializer: JobSerializer by koinLazy()
    private val idGenerator: IdGenerator by koinLazy()
    private val aws: AwsClient by koinLazy()
    private val jobRepository: JobRepository by koinLazy()

    override suspend fun trigger(op: JobTriggerOption): String {
        val jobSk = op.jobSk ?: idGenerator.nextvalAsString()
        val jobParam = jobSerializer.toJson(op, jobSk)
        if (op.preJobPersist) {
            jobSerializer.toJob(jobParam.toString().toGsonData())
        }

        log.trace { "기본적으로 현재 위치가 람다인경우, 로컬로 실행되게 해준다" }
        if (AwsInstanceTypeUtil.INSTANCE_TYPE == AwsInstanceType.LAMBDA) {
            log.info { "현재 인스턴스 타입 = ${AwsInstanceTypeUtil.INSTANCE_TYPE} -> 람다를 다시 트리거하지 않고 로컬(람다)에서 실행됩니다" }
            return JobTriggerMethod.LOCAL.trigger(op)
        }

        if (op.synch) {
            val resultText = aws.lambda.invokeSynch(lambdaFunctionName, jobParam)
            log.info { "람다 실행 [$jobParam] - 동기화(${op.synch}) -> 결과 (${resultText.ok}) ->  결과문자열 ${resultText.result}" }
        } else {
            aws.lambda.invokeAsynch(lambdaFunctionName, jobParam)
            log.info { "람다 실행 [$jobParam] - 동기화(${op.synch})" }
        }
        if (log.isDebugEnabled) {
            val findJob = Job(op.jobPk, jobSk)
            if (op.synch) {
                jobRepository.getItem(findJob)?.let {
                    log.debug { "클라우드와치 로그링크 : ${it.toLogLink()}" }
                    log.debug { "DDB 콘솔 링크 : ${it.toConsoleLink()}" }
                    log.debug { "에러 로그 링크 : ${it.toErrorLogLink()}" }
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

    private val jobSerializer: JobSerializer by koinLazy()
    private val idGenerator: IdGenerator by koinLazy()
    private val aws: AwsClient by koinLazy()

    override suspend fun trigger(op: JobTriggerOption): String {
        val jobSk = op.jobSk ?: idGenerator.nextvalAsString()
        val jobParam = jobSerializer.toJson(op, jobSk)
        if (op.preJobPersist) {
            jobSerializer.toJob(jobParam.toString().toGsonData())
        }
        val findJob = Job(op.jobPk, jobSk)
        if (op.synch) {
            val jobDetail = aws.batch.submitJobAndWaitStarting(jobQueueName, jobDefinitionName, jobParam)
            log.debug { "잡 UI 링크 -> ${BatchUtil.toBatchUiLink(jobDetail.jobId!!)}" }
            log.debug { "잡 DDB 링크 -> ${findJob.toConsoleLink()}" }
        } else {
            val jobId = aws.batch.submitJob(jobQueueName, jobDefinitionName, jobParam)
            log.debug { "잡 UI 링크 -> ${BatchUtil.toBatchUiLink(jobId)}" }
            log.debug { "잡 DDB 링크 -> ${DynamoUtil.toConsoleLink(findJob.tableName, findJob)}" }
        }
        return jobParam.toString()
    }

}