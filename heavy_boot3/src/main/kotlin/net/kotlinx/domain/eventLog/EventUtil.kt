package net.kotlinx.domain.eventLog

import mu.KotlinLogging
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.domain.eventLog.data.EventDataHolder
import net.kotlinx.exception.toSimpleString
import net.kotlinx.json.gson.GsonSet
import net.kotlinx.system.SystemUtil
import net.kotlinx.time.TimeFormat

/**
 * 페이로드 크기: 64KB 청크의 각 페이로드가 1개의 이벤트로 청구 -> 예를 들어 256KB 페이로드의 이벤트는 4개의 요청으로 청구
 * 일반적인 단순 메세지는 1000 byte 정도 함
 */
object EventUtil {

    /** 에러를 request 객체에 임시로 담기 위한 키값 */
    const val ERROR = "error"

    /** 이벤트 처리용 GSON */
    val GSON = GsonSet.BEAN_UTC_ZONE

    /** threadlocal의 데이터를 추가해줌 */
    fun <T : AbstractEvent> initThreadLocalData(event: T): T {
        event.eventId = EventDataHolder.getOrMakeEventId()
        val txTime = EventDataHolder.getTxTime()
        event.eventTime = txTime
        event.eventDate = TimeFormat.YMD[txTime]
        event.data = EventDataHolder.getData()
        event.datas = EventDataHolder.getDatas()

        event.instanceType = AwsInstanceTypeUtil.INSTANCE_TYPE
        event.ip = SystemUtil.IP
        return event
    }

    private val log = KotlinLogging.logger {}

    /** 이벤트 기록 공통. 여기서 발생하는 오류는 일단 무시한다. */
    fun doWithoutException(event: () -> Unit) {
        try {
            event()
        } catch (e: Exception) {
            log.warn { "이벤트 기록중 예외발생 : ${e.toSimpleString()}" }
        }
    }
}