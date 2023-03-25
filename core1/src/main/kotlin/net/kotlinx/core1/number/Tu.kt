package net.kotlinx.core1.number

import java.util.concurrent.TimeUnit

//==================================================== 생성 (접두어 있음) ======================================================
inline fun Number.tuMills(): Tu = Tu(this.toLong(), TimeUnit.MILLISECONDS)
inline fun Number.tuSec(): Tu = Tu(this.toLong(), TimeUnit.SECONDS)
inline fun Number.tuHour(): Tu = Tu(this.toLong(), TimeUnit.HOURS)
inline fun Number.tuMinutes(): Tu = Tu(this.toLong(), TimeUnit.MINUTES)
inline fun Number.tuDays(): Tu = Tu(this.toLong(), TimeUnit.DAYS)

/** 인라인 타임유닛 변환기 */
data class Tu(
    val value: Long,
    val timeUnit: TimeUnit,
) {
    val mills: Long
        get() = timeUnit.toMillis(value)
    val sec: Long
        get() = timeUnit.toSeconds(value)
    val minutes: Long
        get() = timeUnit.toMinutes(value)
    val hours: Long
        get() = timeUnit.toHours(value)
    val days: Long
        get() = timeUnit.toDays(value)
}