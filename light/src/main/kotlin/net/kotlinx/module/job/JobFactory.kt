package net.kotlinx.module.job

import mu.KotlinLogging
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.core.id.IdGenerator
import net.kotlinx.core.lib.SystemUtil
import net.kotlinx.module.job.define.JobDefinitionRepository
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit


interface JobFactory {
    fun create(pk: String): Job
}

class JobFactoryDefault(
    val idGenerator: IdGenerator,
    val jobDefinitionRepository: JobDefinitionRepository,
) : JobFactory {

    private val log = KotlinLogging.logger {}

    override fun create(pk: String): Job {
        return Job(pk, idGenerator.nextval()).apply {
            val jobTrigger = jobDefinitionRepository.findById(pk).jobTriggerMethod
            reqTime = LocalDateTime.now()
            jobStatus = JobStatus.STARTING
            jobEnv = jobTrigger.name
            ttl = when (jobTrigger.jobExeDiv) {
                JobExeDiv.LOCAL -> DynamoUtil.ttlFromNow(TimeUnit.HOURS, 1)  //로컬은 테스트로 간주하고 1시간 보관
                else -> DynamoUtil.ttlFromNow(TimeUnit.DAYS, 7 * 2)
            }
            log.debug { " -> job 신규생성 $pk / $sk" }
            jobExeFromName = "${AwsInstanceTypeUtil.INSTANCE_TYPE.name}/${SystemUtil.IP}" //디버깅 용으로 호출자의 메타데이터를 넣어준다.
        }
    }

}