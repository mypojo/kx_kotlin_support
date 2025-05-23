//package net.kotlinx.domain.batchStep
//
//import mu.KotlinLogging
//import net.kotlinx.aws.lambda.dispatch.asynch.EventBridgeSfnStatus
//import net.kotlinx.collection.toPair
//import net.kotlinx.domain.job.Job
//import net.kotlinx.domain.job.JobRepository
//import net.kotlinx.domain.job.define.JobDefinitionRepository
//import net.kotlinx.domain.job.trigger.JobLocalExecutor
//import net.kotlinx.exception.KnownException
//import net.kotlinx.exception.toSimpleString
//import net.kotlinx.koin.Koins.koin
//import net.kotlinx.reflect.newInstance
//
///**
// * resume 샘플
// * */
//@Deprecated("기본 resmue 활용할것")
//suspend fun JobLocalExecutor.resume(event: EventBridgeSfnStatus, onEventSfnStatusAlert: (EventBridgeSfnStatus) -> Unit = {}) {
//
//    val log = KotlinLogging.logger {}
//
//    log.info { "[${event.sfnName}] resume -> ${event.name} (${event.status})" }
//
//    val (pk, sk) = event.name.substringAfter(".").split(".").toPair()
//
//    val jobRepository = koin<JobRepository>()
//
//    val job = jobRepository.getItem(Job(pk, sk))!!
//    val jobDef = JobDefinitionRepository.findById(job.pk)
//
//    when (event.status) {
//
//        /** 하드코딩했음 */
//        "SUCCEEDED" -> {
//            val jobService = jobDef.jobClass.newInstance()
//
//            //SFN일 경우 SFN잡을 로드해서 컨텍스트를 복사해준다 (처리시간, 비용 등)
//            job.sfnId?.let { sfnId ->
//                val sfnPk = sfnId.substringBefore("-")
//                val sfnSk = sfnId.substringAfter("-") //이후 전체가 SK
//                val sfnJob = jobRepository.getItem(Job(sfnPk, sfnSk))!!
//                job.jobContext = sfnJob.jobContext
//            }
//
//            try {
//                jobService.onProcessComplete(job)
//                this.resumeSuccess(job)
//            } catch (e: KnownException.ItemSkipException) {
//                log.warn { "onProcessComplete 처리중 스킵! -> ${e.toSimpleString()}" }
//            } catch (e: Exception) {
//                log.warn { "onProcessComplete 처리중 예외! -> ${e.toSimpleString()}" }
//                e.printStackTrace()
//                this.resumeFail(job, "onProcessComplete 처리중 예외! -> ${e.toSimpleString()}")
//            }
//
//        }
//
//        else -> {
//            this.resumeFail(job, "${event.status} -> '${event.cause}'")
//            onEventSfnStatusAlert(event)
//        }
//    }
//}
//
