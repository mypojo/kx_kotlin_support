package net.kotlinx.counter

import java.util.concurrent.atomic.AtomicLong

/**
 * 특정 횟수마다 이벤트 트리거가 가능한 간단 카운터
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

    /** 메인이면 실행 */
    fun check(block: (Long) -> Unit) {
        val main = invoke()
        if (!main) return

        block(count.get())
    }
}