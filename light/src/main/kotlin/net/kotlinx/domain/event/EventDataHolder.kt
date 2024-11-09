package net.kotlinx.domain.event

import mu.KotlinLogging
import net.kotlinx.exception.toSimpleString
import net.kotlinx.id.IdGenerator
import net.kotlinx.json.gson.GsonData
import net.kotlinx.koin.Koins.koinLazy
import java.time.LocalDateTime
import kotlin.concurrent.getOrSet

/**
 * 각 로직에 별도로 세팅할것.
 */
object EventDataHolder {

    //==================================================== DATAS ======================================================
    /** 데이터들 보관 (array) */
    private val _DATAS = ThreadLocal<MutableList<EventData>>()

    private val log = KotlinLogging.logger {}

    /**
     * datas 에 추가됨
     * 내부적으로 로직을 감싸서, 실패시 예외를 던지지 않음
     *  */
    fun addData(block: EventData.() -> Unit = {}) {
        try {
            val eventData = EventData().apply(block)
            val datas = _DATAS.getOrSet { mutableListOf() }
            datas.add(eventData)
        } catch (e: Exception) {
            log.error { "  ==> 이벤트 로그데이터 생성중 예외!! ${e.toSimpleString()}" }
            e.printStackTrace()
        }
    }

    val DATAS: List<EventData>
        get() = _DATAS.get() ?: emptyList()

    //==================================================== DATA  ======================================================

    /** 커스텀 데이터들 보관 (object)  */
    private val _BODY = ThreadLocal<GsonData>()

    /** 여기에 커스텀 데이터를 put 하면 됨  */
    val BODY: GsonData
        get() = _BODY.getOrSet { GsonData.obj() }

    //==================================================== TX_TIME ======================================================

    /** 트랜잭션 타임을 미리 박을때 사용 (매출시간 등을 정확하게 일치시켜야할때) */
    private val _TX_TIME = ThreadLocal<LocalDateTime>()

    /** 트랜잭션 타임으 이벤트에 넣어주고 싶을때 사용  */
    fun setTxTime(txTime: LocalDateTime) {
        _TX_TIME.set(txTime)
    }

    val TX_TIME: LocalDateTime
        get() = _TX_TIME.getOrSet { LocalDateTime.now() }

    //==================================================== EVENT_ID ======================================================

    /** 예외 처리 등, 미리 ID를 채번할때 사용  */
    private val _EVENT_ID = ThreadLocal<Long>()

    private val ID_GENERATOR by koinLazy<IdGenerator>()

    val EVENT_ID: Long
        get() = _EVENT_ID.getOrSet { ID_GENERATOR.nextval() }


    //==================================================== 공통 ======================================================

    fun remove() {
        _TX_TIME.remove()
        _DATAS.remove()
        _BODY.remove()
        _EVENT_ID.remove()
    }
}