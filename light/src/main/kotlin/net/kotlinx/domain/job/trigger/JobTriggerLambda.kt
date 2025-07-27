package net.kotlinx.domain.job.trigger

import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.AwsInstanceType
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.lambda.invokeAsynch
import net.kotlinx.aws.lambda.invokeSynch
import net.kotlinx.aws.lambda.lambda
import net.kotlinx.domain.item.errorLog.errorLogQueryLink
import net.kotlinx.domain.job.Job
import net.kotlinx.domain.job.JobExeDiv
import net.kotlinx.domain.job.JobRepository
import net.kotlinx.domain.job.JobStatus
import net.kotlinx.id.IdGenerator
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.koin.Koins

/** 람다 */
class JobTriggerLambda(
    /** 이름 */
    override val name: String,
    /** 호출용 람다 함수 이름 */
    val lambdaFunctionName: String,
) : JobTriggerMethod {

    private val log = KotlinLogging.logger {}

    override val jobExeDiv: JobExeDiv = JobExeDiv.LAMBDA

    private val jobSerializer: JobSerializer by Koins.koinLazy()
    private val idGenerator: IdGenerator by Koins.koinLazy()
    private val aws: AwsClient by Koins.koinLazy()
    private val jobRepository: JobRepository by Koins.koinLazy()

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

        log.trace { "기본적으로 현재 위치가 람다인경우, 람다를 다시 호출하는게 아닌, 현재 람다에서 로컬로 실행되게 해준다" }
        if (op.synch && AwsInstanceTypeUtil.INSTANCE_TYPE == AwsInstanceType.LAMBDA) {
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
            val findJob = Job(op.jobPk, op.jobSk!!)
            if (op.synch) {
                jobRepository.getItem(findJob)?.let {
                    log.debug { "클라우드와치 로그링크 : ${it.cloudWatchLogLink}" }
                    log.debug { "DDB 콘솔 링크 : ${it.dynamoItemLink}" }
                    log.debug { "에러 로그 링크 : ${it.errorLogQueryLink}" }
                }
            }
        }
        return jobParam.toString()
    }

}