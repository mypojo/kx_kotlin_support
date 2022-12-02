package net.kotlinx.kotlinSupport.time

import net.kotlinx.kotlinSupport.time.TimeUtil.toLocalDateTime
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.time.temporal.UnsupportedTemporalTypeException
import java.util.*

/**
 * LocalDateTime 은 모든 포맷이 가능
 * LocalDate 는 시분초를 찍는게 불가능함. 변환 해줄것!
 */
enum class TimeFormat {
    /** 표준 변환이다!  다이나모 DB 등에서 기본으로 사용  */
    ISO(DateTimeFormatter.ISO_DATE_TIME),  //=================================================== 밀리초까지 나오는 풀 시간. 거의 사용되지 않음 ===================================================
    YMDHMSS("yyyyMMddHHmmssSSS"), YMDHMSS_K01("yyyy년MM월dd일(EEE) HH시mm분ss초(SSS)"),  //=================================================== 초까지 나오는 기본 시간  ===================================================
    YMDHMS("yyyyMMddHHmmss"), YMDHMS_F01("yyyy-MM-dd HH:mm:ss"),

    /** mysql 기본형. 2022-07-07 03:12:52.0  */
    YMDHMS_MYSQL("yyyy-MM-dd HH:mm:ss.S"),

    /** 파일 이름에 사용 금지인 : 제거  */
    YMDHMS_F02("yyyy-MM-dd_HH-mm-ss"), YMDHMS_K01("yyyy년MM월dd일(EEE) HH시mm분ss초"),  //=================================================== 분까지 나오는 시간  ===================================================
    YMDHM("yyyyMMddHHmm"), YMDHM_F01("yyyy-MM-dd HH:mm"),

    /** 파일 이름에 사용 금지인 : 제거  */
    YMDHM_F02("yyyy-MM-dd_HH-mm"), YMDHM_K01("yyyy년MM월dd일(EEE) HH시mm분"),  //=================================================== 시간 나오는 시간  ===================================================
    YMDH("yyyyMMddHH"),  //AWS 파이어호스 기본

    //=================================================== 날짜  ===================================================
    YMD("yyyyMMdd"), YMD_F01("yyyy-MM-dd"), YMD_F02("yyyy.MM.dd"), YMD_K01("yyyy년MM월dd일(EEE)"), Y2MD("yyMMdd"), Y2MDHMS("yyMMddMMddHHmmss"), Y2MD_F01("yy-MM-dd"), Y2MD_K01("yy년MM월dd일(EEE)"), YM("yyyyMM"), YM_F01(
        "yyyy-MM"
    ),
    Y("yyyy"),

    /** 날짜 2자리  */
    D("dd"),

    /** 날짜-시분. 배치 네이밍용  */
    DH_F01("dd_HH"),
    //=================================================== 날짜 없는버전  ===================================================
    H("HH"), //패팅 적용
    HM("HHmm"), //패팅 적용
    HMS("HHmmss"),
    HMS_F01("HH:mm:ss"),
    HMSS("HHmmssSSS"),
    HMS_K01("HH시mm분ss초");

    //=================================================== 기본 메소드 ===================================================
    val formatter: DateTimeFormatter

    /** DateTimeFormatter 에는 패턴이 파싱후 삭제된다. 따라서 별도 보관   */
    val pattern: String?

    constructor(pattern: String) {
        formatter = DateTimeFormatter.ofPattern(pattern).withZone(TimeUtil.SEOUL) //서울기준으로 포매팅 한다.
        this.pattern = pattern
    }

    constructor(format: DateTimeFormatter) {
        formatter = format
        pattern = null
    }

    /** 기본 메소드  */
    fun get(): String {
        return formatter.format(LocalDateTime.now())
    }

    /** 기본 메소드  */
    operator fun get(mills: Long): String {
        val localDateTime = toLocalDateTime(mills)
        return formatter.format(localDateTime)
    }

    /** 기본 메소드  */
    operator fun get(date: Date): String {
        return this[date.time]
    }

    /** 기본 메소드  */
    operator fun get(dateTime: LocalDateTime?): String {
        return if (dateTime == null) "" else formatter.format(dateTime)
    }

    /** 기본 메소드  */
    operator fun get(instant: Instant?): String {
        return formatter.format(instant)
    }

    /** 기본 메소드  */
    operator fun get(dateTime: LocalDate): String {
        return try {
            formatter.format(dateTime)
        } catch (e: UnsupportedTemporalTypeException) {
            val localDateTime = dateTime.atTime(LocalTime.MIN)
            formatter.format(localDateTime)
        }
    }

    /** 기본 메소드  */
    operator fun get(dateTime: LocalTime?): String {
        return formatter.format(dateTime)
    }

    /** 기본 메소드  */
    operator fun get(dateTime: String?): TemporalAccessor {
        return formatter.parse(dateTime)
    }

    //==================================================== 편의용 변환 메소드 (표준) ======================================================
    fun toLocalTime(dateTime: String?): LocalTime {
        val data = formatter.parse(dateTime)
        return LocalTime.from(data)
    }

    /** DATE를 DATETIME으로 변환하려고 하면 에러난다. 이걸 우선으로 사용할것  */
    fun toLocalDate(dateTime: String?): LocalDate {
        val data = formatter.parse(dateTime)
        return LocalDate.from(data)
    }

    /**
     * 캐스트업이 필요한 경우 최소값을 입력해준다.
     * ex) 2020년 01월 => 2020년 01월 1일 00:00
     */
    fun toLocalDateTime(dateTime: String?): LocalDateTime {
        val data = formatter.parse(dateTime)
        return try {
            LocalDateTime.from(data)
        } catch (e: DateTimeException) {
            try {
                val localDate = LocalDate.from(data)
                localDate.atTime(LocalTime.MIN)
            } catch (exception: Exception) {
                val localDate = YearMonth.from(data)
                localDate.atDay(1).atTime(LocalTime.MIN)
            }
        }
    }

    companion object {
        /** from 으로 파싱해서 to 로 출력함  */
        fun convert(dateTime: String?, from: TimeFormat, to: TimeFormat): String {
            val localDateTime = from.toLocalDateTime(dateTime)
            return to[localDateTime]
        }
    }
}