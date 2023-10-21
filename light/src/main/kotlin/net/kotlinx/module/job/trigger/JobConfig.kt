package net.kotlinx.module.job.trigger

import com.google.common.eventbus.EventBus
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.AwsInfoLoader
import net.kotlinx.module.job.JobFactory
import net.kotlinx.module.job.JobRepository
import net.kotlinx.module.job.define.JobDefinitionRepository


/**
 * 잡 설정 모음집
 */
class JobConfig(
    aws: AwsClient1,
    awsInfoLoader: AwsInfoLoader,
    eventBus: EventBus,
    val jobDefinitionRepository: JobDefinitionRepository,
    val jobFactory: JobFactory
) {

    val jobRepository = JobRepository(aws)

    val jobSerializer = JobSerializer(jobRepository, jobFactory)

    val jobLocalExecutor = JobLocalExecutor(jobRepository, jobDefinitionRepository, eventBus, awsInfoLoader)

    val jobTrigger = JobTrigger(
        jobFactory,
        jobRepository,
        jobSerializer,
        aws,
        jobLocalExecutor,
        jobDefinitionRepository,
    )

}

