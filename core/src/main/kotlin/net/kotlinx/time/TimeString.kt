package net.kotlinx.time

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.system.measureTimeMillis

/**
 * 시분초를 문자열로 바꿔준다.
 * 간단 로그 출렬용
 */
data class TimeString(
    val millis: Long,
) {

    private val totalSecond = millis / 1000
    private val hour = totalSecond / 60 / 60
    private val min = (totalSecond - hour * 60 * 60) / 60
    private val sec = totalSecond % 60

    /** 시분초를 나누어 문자열을 제작한다. 24시간이 넘을 경우 적절히 조절한다.  */
    override fun toString(): String {
        if (hour > 24) {
            val hour = hour % 24
            var day = this.hour / 24
            return if (day > 365) {
                val year = day / 365
                day %= 365
                "${year}년 ${day}일 ${hour}시간"
            } else {
                "${day}일 ${hour}시간 ${min}분"
            }
        }
        if (hour != 0L) return "${hour}시간 ${min}분 ${sec}초"
        if (min != 0L) return "${min}분 ${sec}초"
        if (sec > 10L) return "${sec}초"
        return if (millis >= 100) {
            //0.10초 까지 표현
            "${BigDecimal.valueOf(millis.toDouble() / 1000).setScale(1, RoundingMode.HALF_UP)}초"
        } else {
            "${millis}밀리초"
        }
    }
}

/** 가능하면 measureTime을 사용하자 */
data class TimeStart(
    private val start: Long = System.currentTimeMillis(),
) {

    /** 지금 기준 시작과의  차이 */
    fun interval(): Long = System.currentTimeMillis() - start

    override fun toString(): String {
        return TimeString(interval()).toString()
    }
}

/** 밀리초로 간주하고 문자로 변환 (로그 확인용) */
fun Long.toTimeString(): TimeString = TimeString(this)

/** suspend 지원하는 시간 측정기 */
fun measureTimeString(block: suspend () -> Unit): TimeString = measureTimeMillis {
    runBlocking { block() }
}.toTimeString()

/** suspend 지원하는 시간 측정기 */
fun measureTimePrint(block: suspend () -> Unit) {
    val start = TimeStart()
    runBlocking { block() }
    val log = KotlinLogging.logger {}
    log.warn { "걸린시간 : ${start}" }
}

/** suspend 지원하는 시간 측정기 - 간단버전 */
fun measureTime(block: suspend () -> Any?): Any? {
    val log = KotlinLogging.logger {}
    var result: Any? = null
    val millis = measureTimeMillis {
        result = runBlocking { block() }
    }
    log.debug { " -> ${millis.toTimeString()}" }
    return result
}