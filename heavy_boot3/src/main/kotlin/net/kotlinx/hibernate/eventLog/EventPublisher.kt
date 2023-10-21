package net.kotlinx.hibernate.eventLog

import aws.sdk.kotlin.services.eventbridge.EventBridgeClient
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.eventBridge.EventBridgeConfig
import net.kotlinx.aws.eventBridge.putEvents
import net.kotlinx.core.gson.GsonSet
import net.kotlinx.core.lib.toSimpleString
import net.kotlinx.hibernate.eventLog.data.EventDataHolder


/**
 * 이벤트를 이벤트브릿지로 전송해준다.
 * 설치되는곳
 * web : spring filter 에 추가
 * job : JobRunner 의 종료 설정에 추가
 * */
class EventPublisher(
    private val eventBridgeClient: EventBridgeClient,
    private val eventBridgeConfig: EventBridgeConfig,
    /** 한번에 보낼 최대 데이터 수 (이벤트브릿지 용량제한때문) */
    private val entryLimit: Int = 50,
) {

    private val log = KotlinLogging.logger {}

    /** 여기서 발생하는 오류는 일단 무시한다. */
    fun <T : AbstractEvent> pub(block: () -> T?) {
        try {
            val event = block() ?: return //null이면 무시
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

    private suspend fun doPubEach(event: AbstractEvent) {
        val json = EventUtil.GSON.toJson(event)!!
        if (log.isTraceEnabled) {
            log.debug { " -> event json \n${GsonSet.GSON_PRETTY.toJson(event)}" }
        }
        log.info { "event 발송.. [${event.eventDiv}] 데이터 ${event.datas.size}건 (${json.toByteArray().size}byte)" }
        eventBridgeClient.putEvents(eventBridgeConfig, listOf(json))
    }

}