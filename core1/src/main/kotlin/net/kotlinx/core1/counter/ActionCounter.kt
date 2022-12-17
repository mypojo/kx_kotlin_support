package net.kotlinx.core1.counter

import java.util.concurrent.atomic.AtomicLong
import java.util.function.Consumer

/**
 * 특정 횟수마다 이벤트 트리거가 가능한 간단 카운터
 */
class ActionCounter(
    /** 액션이 실행될 카운트 수  */
    private val action: Long,
    /** 이벤트마다 증가하는 카운터 겸 ID  */
    private val event: Consumer<Long>
) {
    private val count = AtomicLong()

    fun incrementAndGet(): Long {
        val count = count.incrementAndGet()
        if (count % action == 0L) {
            event.accept(count)
        }
        return count
    }
}