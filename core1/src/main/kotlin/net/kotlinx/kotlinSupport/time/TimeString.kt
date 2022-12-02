package net.kotlinx.kotlinSupport.time

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 시분초를 문자열로 바꿔준다.
 */
data class TimeString(
    private val millis: Long,
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