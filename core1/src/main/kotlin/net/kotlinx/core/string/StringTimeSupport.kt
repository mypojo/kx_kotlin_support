package net.kotlinx.core.string

import net.kotlinx.core.regex.RegexSet
import net.kotlinx.core.time.TimeFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/** 공용 날짜 변환 : 로그 확인용. 시분초 이상 더 필요하면 추가코딩 */
inline fun String.toLocalDateTime():LocalDateTime{
    //일단 ISO 스타일 먼저 체크
    if(this.contains('T')){

        return TimeFormat.ISO.toLocalDateTime(this) //이경우 존 정보는 버린다.
    }
    val value = this.retainFrom(RegexSet.NUMERIC)
    return when(value.length){
        6 -> TimeFormat.YM.toLocalDateTime(value)
        8 -> TimeFormat.YMD.toLocalDateTime(value)
        10 -> TimeFormat.YMDH.toLocalDateTime(value)
        12 -> TimeFormat.YMDHM.toLocalDateTime(value)
        14 -> TimeFormat.YMDHMS.toLocalDateTime(value)
        17 -> TimeFormat.YMDHMSS.toLocalDateTime(value)
        else -> throw IllegalArgumentException("invalid date format : $value")
    }
}

/** 공용 날짜 변환 : 로그 확인용 */
inline fun String.toLocalDate():LocalDate = TimeFormat.YMD.toLocalDate(this.retainFrom(RegexSet.NUMERIC))
/** 공용 날짜 변환 : 로그 확인용 */
inline fun String.toLocalTime(): LocalTime = TimeFormat.HMS.toLocalTime(this.retainFrom(RegexSet.NUMERIC))
