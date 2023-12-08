package net.kotlinx.core.number

import net.kotlinx.core.time.TimeUtil
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * @see BigDecimal.halfUp
 *  */
inline fun Long.halfUp(scale: Int): BigDecimal = this.toBigDecimal().halfUp(scale)


/** coerceAtLeast 단어가 너무 헷갈려서 재정의함 */
inline fun Long.maxWith(compare: Long): Long = this.coerceAtLeast(compare)

/** coerceAtLeast 단어가 너무 헷갈려서 재정의함 */
inline fun Long.minWith(compare: Long): Long = this.coerceAtMost(compare)

/**
 * 밀리초를 시간으로 변환
 * ex) 1681869805.seconds.inWholeMilliseconds.toLocalDateTime().toKr01()
 *  */
inline fun Long.toLocalDateTime(zone: ZoneId = TimeUtil.SEOUL): LocalDateTime = Instant.ofEpochMilli(this).atZone(zone).toLocalDateTime()!!

/**
 * 비율을 구할때.
 * 수식 연산이 많아진다면 DSL을 사용할것
 *  */
inline fun Long.toRate(sum: Long, scale: Int = 1): BigDecimal {
    if (sum == 0L) return BigDecimal.ZERO
    return (this * 100.0 / sum).halfUp(scale)
}

/** 기본 div는 이미 있음 */
inline fun Long.div2(value: Long): Long {
    if (value == 0L) return 0L
    return this / value
}

/** 만원단위 변환. 은근히 자주 사용됨 */
inline fun Long.toManwon(scale: Int = 1): BigDecimal = (this / 10000.0).halfUp(scale)