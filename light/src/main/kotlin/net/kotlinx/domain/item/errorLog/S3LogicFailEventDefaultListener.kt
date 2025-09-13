package net.kotlinx.domain.item.errorLog

import com.google.common.eventbus.Subscribe
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.aws.lambda.dispatch.synch.s3Logic.S3LogicFailEvent
import net.kotlinx.core.Kdsl
import net.kotlinx.exception.toSimpleString
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.time.truncatedMills
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Duration.Companion.days


/**
 * 디폴트 이벤트 리스너
 * 커스텀해서 사용해주세요
 *  */
class S3LogicFailEventDefaultListener {

    private val log = KotlinLogging.logger {}

    private val errorLogRepository by koinLazy<ErrorLogRepository>()

    @Kdsl
    constructor(block: S3LogicFailEventDefaultListener.() -> Unit = {}) {
        apply(block)
    }

    /** 로그 지속시간 */
    var ttlDuraton = 7.days

    @Subscribe
    fun onEvent(event: S3LogicFailEvent) {

        log.trace { "S3Logic은 리트라이 하지 않음으로 예외를 던지지 않고 실패 리턴함 (로그 지저분해짐)" }
        if (log.isDebugEnabled) {
            event.e.printStackTrace()
        }
        log.warn { "###### 데이터 처리 실패!! 알람 보내지 않고 넘어감 -> ${event.path.s3InputDataKey} / ${event.path.pathId} -> ${event.e.toSimpleString()}" }

        //ex) path =  upload/sfnBatchModuleInput/batchTaskExecutor-job.kwdDemoMapJob.49610001/00000.txt
        //ex) pathId = batchTaskExecutor-job.kwdDemoMapJob.49610001
        val sfnId = event.path.pathId
        val (group, div, divId) = sfnId.substringAfter("-").split(".")

        val errorLog = ErrorLog(
            group = group,  //job
            div = div,  //kwdDemoMapJob
            divId = divId, //49610001
            id = UUID.randomUUID().toString(),
            ttl = DynamoUtil.ttlFromNow(ttlDuraton),
            time = LocalDateTime.now().truncatedMills(),
            cause = event.e.toSimpleString(),
            stackTrace = event.e.stackTraceToString(),
        )
        runBlocking { errorLogRepository.putItem(errorLog) }

    }

}