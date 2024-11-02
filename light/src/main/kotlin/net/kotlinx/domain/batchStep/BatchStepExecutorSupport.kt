package net.kotlinx.domain.batchStep

import net.kotlinx.aws.lambda.dispatch.asynch.EventBridgeSfnStatus
import net.kotlinx.collection.toPair
import net.kotlinx.domain.job.Job
import net.kotlinx.domain.job.JobRepository
import net.kotlinx.domain.job.define.JobDefinitionRepository
import net.kotlinx.domain.job.trigger.JobLocalExecutor
import net.kotlinx.koin.Koins.koin
import net.kotlinx.reflect.newInstance

/**
 * resume 샘플
 * */
suspend fun JobLocalExecutor.resume(event: EventBridgeSfnStatus, onEventSfnStatusAlert: (EventBridgeSfnStatus) -> Unit = {}) {
    val (pk, sk) = event.name.substringAfter(".").split(".").toPair()

    val jobRepository = koin<JobRepository>()

    val job = jobRepository.getItem(Job(pk, sk))!!
    val jobDef = JobDefinitionRepository.findById(job.pk)

    when (event.status) {

        /** 하드코딩했음 */
        "SUCCEEDED" -> {
            val jobService = jobDef.jobClass.newInstance()

            //SFN일 경우 SFN잡을 로드해서 컨텍스트를 복사해준다 (처리시간, 비용 등)
            job.sfnId?.let { sfnId ->
                val sfnPk = sfnId.substringBefore("-")
                val sfnSk = sfnId.substringAfter("-") //이후 전체가 SK
                val sfnJob = jobRepository.getItem(Job(sfnPk, sfnSk))!!
                job.jobContext = sfnJob.jobContext
            }

            jobService.onProcessComplete(job)
            this.resumeSuccess(job)
        }

        else -> {
            this.resumeFail(job, "${event.status} -> '${event.cause}'")
            onEventSfnStatusAlert(event)
        }
    }
}

