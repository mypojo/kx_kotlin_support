package net.kotlinx.core2.concurrent

import net.kotlinx.core.number.minWith
import java.util.function.LongBinaryOperator
import kotlin.math.max
import kotlin.math.min

/**
 * AtomicLong 에서 지원하는 오퍼레이터 모음
 */
object AtomicLongOperatorSet {

    /** ex) getAndAccumulate  */
    val MAX = LongBinaryOperator { a: Long, b: Long -> max(a, b) }

    /** ex) getAndAccumulate  */
    val MIN = LongBinaryOperator { a: Long, b: Long -> min(a, b) }

    /**
     * 0 이하는 무시. &  left가 0이라면 0 이상인값 통과
     * 각종 숫자에서 양수인 최소값을 찾을때 사용한다.
     */
    val MIN_INT = LongBinaryOperator { exist: Long, input: Long ->
        if (input <= 0L) return@LongBinaryOperator exist
        if (exist == 0L) return@LongBinaryOperator input
        exist.minWith(input)
    }
}
