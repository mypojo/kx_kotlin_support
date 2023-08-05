package net.kotlinx.module.eventLog.data

import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.id.IdGenerator
import java.time.LocalDateTime

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
        val datas = DATAS.get() ?: let { mutableListOf<EventData>().also { DATAS.set(it) } }
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
    fun getTxTime(): LocalDateTime {
        return TX_TIME.get() ?: let { LocalDateTime.now().also { TX_TIME.set(it) } }
    }

    //==================================================== EVENT_ID ======================================================

    /** 예외 처리 등, 미리 ID를 채번할때 사용  */
    private val EVENT_ID = ThreadLocal<Long>()

    private lateinit var idGenerator: IdGenerator

    /** 미리 세팅 안하면 null 리턴됨  */
    fun getOrMakeEventId(): Long {
        return EVENT_ID.get() ?: let { idGenerator.nextval().also { EVENT_ID.set(it) } }
    }

    //==================================================== 공통 ======================================================

    fun init(idGenerator: IdGenerator) {
        this.idGenerator = idGenerator
    }

    fun remove() {
        TX_TIME.remove()
        DATAS.remove()
        DATA.remove()
        EVENT_ID.remove()
    }
}