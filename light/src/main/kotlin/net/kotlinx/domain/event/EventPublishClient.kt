package net.kotlinx.domain.event

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.AwsInstanceTypeUtil
import net.kotlinx.aws.eventBridge.EventBridgeConfig
import net.kotlinx.aws.eventBridge.event
import net.kotlinx.aws.eventBridge.putEvents
import net.kotlinx.core.Kdsl
import net.kotlinx.exception.toSimpleString
import net.kotlinx.json.gson.GsonSet
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.system.SystemUtil
import net.kotlinx.time.TimeFormat


/**
 * 이벤트를 이벤트브릿지로 전송해준다.
 * 설치되는곳
 * web : spring filter 에 추가
 * job : JobRunner 의 종료 설정에 추가
 * */
class EventPublishClient {

    private val log = KotlinLogging.logger {}

    @Kdsl
    constructor(block: EventPublishClient.() -> Unit = {}) {
        apply(block)
    }

    private val aws by koinLazy<AwsClient>()

    /**
     * 하나의 시스템에는 보통 1개의 이벤트브릿지만 사용함 -> 이때문에 설정을 한개로 고정
     *  */
    lateinit var eventBridgeConfig: EventBridgeConfig

    /** 이벤트 처리용 GSON */
    var gson = GsonSet.TABLE_UTC_WITH_ZONE

    /**
     * 한번에 보낼 최대 데이터 수 (이벤트브릿지 용량제한때문)
     * 편의상 용량 크기가 아니라 갯수로 구분함
     *  */
    var entryLimit: Int = 50

    /**
     * 디폴드 생성 로직.
     * */
    var defaultFactory: () -> Event = {
        Event {
            eventId = EventDataHolder.EVENT_ID

            val txTime = EventDataHolder.TX_TIME
            eventTime = txTime
            eventDate = TimeFormat.YMD[txTime]

            body = EventDataHolder.BODY
            datas = EventDataHolder.DATAS

            instanceType = AwsInstanceTypeUtil.INSTANCE_TYPE
            ip = SystemUtil.IP
        }
    }

    /**
     * 여기서 발생하는 오류는 일단 무시.
     * 디폴트 생성후, 커스텀하게 업데이트 가능
     *  */
    fun pub(eventBuilder: (Event) -> Boolean) {
        try {

            val event = defaultFactory()

            val ok = eventBuilder(event)
            if (!ok) return

            runBlocking {
                if (event.datas.size >= entryLimit) {
                    log.warn { "이벤트 크기가 커서 분할 합니다. (최대 256kb) 데이터 ${event.datas.size}건" }
                    event.datas.chunked(entryLimit).forEach {
                        event.datas = it
                        doPubEach(event) //주의. 객체 그냥 재사용함. 나중에 data class로 수정할것.
                    }
                } else {
                    doPubEach(event)
                }
            }

        } catch (e: Throwable) {
            log.warn { "이벤트 기록중 예외발생 : ${e.toSimpleString()}" }
        } finally {
            EventDataHolder.remove()
        }
    }

    private suspend fun doPubEach(event: Event) {
        val json = gson.toJson(event)!!
        if (log.isTraceEnabled) {
            log.debug { " -> eventbridge json \n${GsonSet.GSON_PRETTY.toJson(event)}" }
        }
        log.info { "eventbridge put -> [${event.eventDiv}] 내부데이터 ${event.datas.size}건 (${json.toByteArray().size}byte)" }
        aws.event.putEvents(eventBridgeConfig, listOf(json))
    }

}