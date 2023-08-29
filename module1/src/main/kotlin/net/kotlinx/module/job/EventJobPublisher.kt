package net.kotlinx.module.job//package net.kotlinx.module.job.metadata
//
//class EventJobPublisher(
//    private val eventBridge: AwsEventBridgeClient
//) {
//
//    private val log = LoggerFactory.getLogger(javaClass)
//
//    fun pubEventJob(job: Job) {
//        try {
//            doPub(job)
//        } catch (e: Throwable) {
//            log.warn("이벤트 기록중 예외 : {},", ExceptionUtil.toString(e), e)
//        } finally {
//            EventDataHolder.remove()
//        }
//    }
//
//    private fun doPub(job: Job) {
//        val eventJob = EventUtil.updateEvent(EventJob())
//
//        //web / job 공용
//        eventJob.eventId = EventDataHolder.getOrMakeEventId(MyInstances.I.idGenerator())
//        eventJob.eventMills = job.toIntervalMills()
//        eventJob.instanceType = AwsInstanceTypeUtil.instanceType
//        eventJob.ip = SystemUtil.getServerIp()
//        //eventJob.logLink = MyInstances.I.jobFactory().createJob().toLogLink() //????
//        eventJob.logLink = job.toLogLink()
//        eventJob.eventStatus = job.jobStatus.toString()
//        eventJob.errMsg = job.jobErrMsg
//        eventJob.eventDiv = job.pk
//        eventJob.eventHash = job.sk
//        eventJob.eventName = job.jobComment
//        eventJob.author = job.authors?.joinToString(",")
//
//        //job 전용
//        eventJob.jobExeDiv = job.jobExeDiv?.toString()
//        eventJob.memberId = job.memberId
//        eventJob.rowTotalCnt = job.rowTotalCnt
//        eventJob.rowSuccessCnt = job.rowSuccessCnt
//        eventJob.rowFailCnt = job.rowFailCnt
//
//        if (eventJob.datas!!.size >= ENTRY_LIMIT) {
//            log.warn("이벤트 크기가 커서 분할 합니다. (최대 256kb) 데이터 ${eventJob.datas!!.size}건")
//            eventJob.datas!!.chunked(ENTRY_LIMIT).forEach {
//                eventJob.datas = it
//                doPubEach(eventJob) //주의. 객체 그냥 재사용함. 나중에 data class로 수정할것.
//            }
//        } else {
//            doPubEach(eventJob)
//        }
//
//
//    }
//
//    private fun doPubEach(eventJob: EventJob) {
//        val json = EventUtil.toJson(eventJob)
//        if (log.isTraceEnabled) {
//            log.debug(" -> event json \n{}", GsonUtil.parsePrettyOrIgnore(json))
//        }
//        log.info("event 발송.. [${eventJob.eventDiv}] 데이터 ${eventJob.datas!!.size}건 (${json.toByteArray().size}byte)")
//        eventBridge.putEvents(job, json)
//    }
//
//
//}