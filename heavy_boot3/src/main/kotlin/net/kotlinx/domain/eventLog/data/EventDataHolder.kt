package net.kotlinx.domain.eventLog.data

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
    private val DATAS = ThreadLocal<MutableList<EventData>>()

    /** datas 에 추가됨 */
    fun addData(block: EventData.() -> Unit = {}) {
        val eventData = EventData().apply(block)
        val datas = DATAS.getOrSet { mutableListOf() }
        datas.add(eventData)
    }

    fun getDatas(): List<EventData> = DATAS.get() ?: emptyList()

    //==================================================== DATA  ======================================================

    /** 커스텀 데이터들 보관 (object)  */
    private val DATA = ThreadLocal<GsonData>()

    /** 여기에 커스텀 데이터를 put 하면 됨  */
    fun getData(): GsonData {
        return DATA.get() ?: let { GsonData.obj().also { DATA.set(it) } }
    }

    //==================================================== TX_TIME ======================================================

    /** 트랜잭션 타임을 미리 박을때 사용 (매출시간 등을 정확하게 일치시켜야할때) */
    private val TX_TIME = ThreadLocal<LocalDateTime>()

    /** 트랜잭션 타임으 이벤트에 넣어주고 싶을때 사용  */
    fun setTxTime(txTime: LocalDateTime) {
        TX_TIME.set(txTime)
    }

    /** tx당 1개 생성되는 표준 시간을 리턴  */
    fun getTxTime(): LocalDateTime = TX_TIME.getOrSet { LocalDateTime.now() }

    //==================================================== EVENT_ID ======================================================

    /** 예외 처리 등, 미리 ID를 채번할때 사용  */
    private val EVENT_ID = ThreadLocal<Long>()

    private val ID_GENERATOR by koinLazy<IdGenerator>()

    /** 미리 세팅 안하면 null 리턴됨  */
    fun getOrMakeEventId(): Long = EVENT_ID.getOrSet { ID_GENERATOR.nextval() }

    //==================================================== 공통 ======================================================

    fun remove() {
        TX_TIME.remove()
        DATAS.remove()
        DATA.remove()
        EVENT_ID.remove()
    }
}