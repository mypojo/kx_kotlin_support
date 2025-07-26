package net.kotlinx.string

import net.kotlinx.regex.RegexSet
import net.kotlinx.regex.retainFrom
import net.kotlinx.time.TimeFormat
import net.kotlinx.time.TimeUtil
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 공용 Local 날짜 변환
 * 주의!! 존이 입력될경우 무조건 한국 시간으로 변환한다.
 *  */
fun String.toLocalDateTime(): LocalDateTime {
    //일단 ISO 스타일 먼저 체크
    if (this.contains('T')) {

        //ISO 의 경우 자동변환을 지원하지 않고나 오류가 많다. 직접 설정함!
        return when {
            this.contains("+") -> {
                val text = this.replace("+0000", "+00:00")  //0000은 못읽는다..
                TimeFormat.ISO.toZonedDateTime(text).withZoneSameInstant(TimeUtil.SEOUL).toLocalDateTime() //무조건 한국시간으로 바꾼다.
            }

            else -> TimeFormat.ISO.toLocalDateTime(this) // 명시적인 + 표기가 없으면 한국시간으로 간주
        }
    }

    val value = this.retainFrom(RegexSet.NUMERIC)
    return when (value.length) {
        6 -> TimeFormat.YM.toLocalDateTime(value)
        8 -> TimeFormat.YMD.toLocalDateTime(value)
        10 -> TimeFormat.YMDH.toLocalDateTime(value)
        12 -> TimeFormat.YMDHM.toLocalDateTime(value)
        14 -> TimeFormat.YMDHMS.toLocalDateTime(value)
        17 -> TimeFormat.YMDHMSS.toLocalDateTime(value)
        else -> throw IllegalArgumentException("invalid date format : $value")
    }
}

/**
 * 공용 날짜 변환.
 * ex) 로그 확인용
 * */
fun String.toLocalDate(): LocalDate {
    val value = this.retainFrom(RegexSet.NUMERIC)
    return when (value.length) {
        8 -> TimeFormat.YMD.toLocalDate(value)
        else -> throw IllegalArgumentException("invalid date format : $value")
    }

}

/**
 * 공용 시간 변환
 * ex) 로그 확인용
 * */
fun String.toLocalTime(): LocalTime {
    val value = this.retainFrom(RegexSet.NUMERIC)
    return when (value.length) {
        5 -> TimeFormat.HMS.toLocalTime(value.padStart(6, '0')) //5자리는 앞에 0을 채워줌
        6 -> TimeFormat.HMS.toLocalTime(value)
        else -> throw IllegalArgumentException("invalid datetime format : $value")
    }
}
