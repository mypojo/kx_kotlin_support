package net.kotlinx.aws.lambda.dispatch.synch.s3Logic

import com.google.common.eventbus.Subscribe
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.domain.ddb.DdbBasicRepository
import net.kotlinx.domain.ddb.errorLog.ErrorLog
import net.kotlinx.domain.ddb.errorLog.ErrorLogConverter
import net.kotlinx.exception.toSimpleString
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

    private val repository by lazy { DdbBasicRepository(null, ErrorLogConverter()) }

    /** 로그 지속시간 */
    var ttlDuraton = 1.days

    @Subscribe
    fun onEvent(event: S3LogicFailEvent) {

        log.trace { "S3Logic은 리트라이 하지 않음으로 예외를 던지지 않고 실패 리턴함 (로그 지저분해짐)" }
        if (log.isDebugEnabled) {
            event.e.printStackTrace()
        }
        log.warn { "###### 데이터 처리 실패!! 알람 보내지 않고 넘어감 -> ${event.path.fileName} / ${event.e.toSimpleString()}" }

        //ex) path =  upload/sfnBatchModuleInput/batchTaskExecutor-job.kwdDemoMapJob.49610001/00000.txt
        //ex) pathId = batchTaskExecutor-job.kwdDemoMapJob.49610001
        val sfnId = event.path.pathId
        val paths = sfnId.substringAfter("-").split(".")
        check(paths.size == 3)

        val errorLog = ErrorLog {
            group = paths[0]
            div = paths[1]
            divId = paths[2]
            id = UUID.randomUUID().toString()
            time = LocalDateTime.now().truncatedMills()
            cause = event.e.toSimpleString()
            stackTrace = event.e.stackTraceToString()
            ttl = DynamoUtil.ttlFromNow(ttlDuraton)
        }
        runBlocking { repository.putItem(errorLog) }

    }

}