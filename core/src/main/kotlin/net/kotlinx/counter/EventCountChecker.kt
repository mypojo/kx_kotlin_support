package net.kotlinx.counter

import java.util.concurrent.atomic.AtomicLong

/**
 * 특정 횟수마다 이벤트 트리거가 가능한 간단 카운터
 * ex) 매 x 회마다 로깅 남김
 */
class EventCountChecker(
    /** 액션이 실행될 회차  */
    private val limit: Long,
) : () -> Boolean {

    private val count = AtomicLong()

    override fun invoke(): Boolean {
        val count = count.incrementAndGet()
        return count % limit == 0L
    }

    /** x회인경우 실행 */
    fun check(block: (Long) -> Unit) {
        val main = invoke()
        if (!main) return

        block(count.get())
    }

    /** 서스펜드 버전 */
    suspend fun checks(block: suspend (Long) -> Unit) {
        val main = invoke()
        if (!main) return

        block(count.get())
    }

    /**
     * 메인이면 실행 후 리턴
     *  */
    fun <T> call(block: (Long) -> T): T? {
        val main = invoke()
        if (!main) return null

        return block(count.get())
    }

    private val latch: Latch = Latch()

    /**
     *  최초 or 메인이면 실행 후 리턴
     *  */
    fun <T> callOrFirst(block: (Long) -> T): T? {
        if (latch.check()) {
            return block(count.get())
        }
        return call(block)

    }
}