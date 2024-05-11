package net.kotlinx.time

import net.kotlinx.string.toLocalDate
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * 모르는건 기본 API 먼저 확인할것
 *
 *
 * @see Instant <--
 * @see LocalDateTime <-- 타임존이 적용된 일상 생활에 쓰이는 시간.
 *
 * @see java.time.temporal.ChronoUnit  <-- ex) LocalDateTime.now
 * @see Duration  <-- between 등
 *
 * @see Period
 *
 * ex)
 * 달의 마지막 날짜
 * lastMonth.with
 */
object TimeUtil {

    private const val ASIA_SEOUL = "Asia/Seoul"

    val SEOUL: ZoneId = ZoneId.of(ASIA_SEOUL)
    private val ZONE_SEOUL = TimeZone.getTimeZone(SEOUL)

    val UTC: ZoneId = ZoneId.of("UTC")

    /** 디폴트 타임존 고정  */
    fun initTimeZone(timeZone: TimeZone = ZONE_SEOUL) {
        TimeZone.setDefault(timeZone)
    }


    //==================================================== 텍스트 파싱 ======================================================
    /**
     * 디폴트 파싱
     * ex) 2022-01-20T14:15:27.394237800
     */
    fun parseTime(time: String): LocalDateTime {
        return LocalDateTime.parse(time)
    }

    /**
     * 타임 존이 없는 UTC 베이스의 Instance 반환용
     * ex) 2021-10-07T01:40:53Z
     * 네이버가 이렇게 준다.
     * 타임존이 필요하면 instant.atZone(TimeUtil.SEOUL) 이렇게 변환할것.
     */
    fun parseTimeUtc(time: String): Instant {
        return Instant.parse(time)
    }

    /**
     * 한국 시간에 존을 적용해서 포매팅한다. 한국시도 편하게 봐야 함으로 OFFSET 를 사용함 (반드시 존 정보가 들어가야함)
     * 주로 athena 등에 입력하 용도로 사용
     * ex) 2022-09-15T09:09:30.617838+09:00
     */
    fun toIsoInstant(time: LocalDateTime): String {
        val zonedDateTime = time.atZone(SEOUL)
        return toIsoInstant(zonedDateTime)
    }

    fun toIsoInstant(time: ZonedDateTime): String {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(time)
    }

    /** 간단버전 */
    fun between(range: Pair<String, String>): List<String> = between(range.first.toLocalDate(), range.second.toLocalDate()).map { it.toYmd() }

    /** 향후 kotlin의 LocalDate 로 변경하기위해서 임시로만 작업함 */
    fun between(start: LocalDate, end: LocalDate): List<LocalDate> {
        check(start <= end) { "시작 날짜보다 종료 날짜가 더 커야합니다" }
        val list = mutableListOf<LocalDate>()
        var current = start
        while (true) {
            list.add(current)
            if (current >= end) break
            current = current.plusDays(1)
        }
        return list
    }

    /**
     * 해당 날짜 기준으로, 주의 시작날짜(월요일)
     * ex) 주 1회 작동하는 기능의 pk
     *  */
    fun firstDateOfWeek(date: LocalDate = LocalDate.now()): LocalDate = date.minusDays(date.dayOfWeek.value - 1L)

    //
    //    /**
    //     * 까먹을까봐 여기 정리. 남는값은 버림한다.
    //     * "일" 로 trim()하면 new LocalDate() 와 동일해진다.
    //     * new LocalDate().toDate() == trim(dateTime,DateTimeFieldType.dayOfYear())
    //     *  */
    //    public static DateTime trim(DateTime dateTime,DateTimeFieldType type){
    //        return dateTime.property(type).roundFloorCopy();
    //    }
    //
    //    /** 10분 단위로 트림한다. 특수용도. 시작시간과 무관하게 자동입찰시 타임슬롯을 규칙적으로 구하기 위해 만듬 . 범용적으로 만들 수 있을거 같은데 귀찮아서 안함 */
    //    public static DateTime trimWith10min(DateTime dateTime){
    //        int min = dateTime.getMinuteOfHour();
    //        int remain = min % 10;
    //        return trim(dateTime.minusMinutes(remain),DateTimeFieldType.minuteOfHour());
    //    }
    //
    //    /** 5분 단위로 트림한다. 특수용도. 시작시간과 무관하게 자동입찰시 타임슬롯을 규칙적으로 구하기 위해 만듬 . 범용적으로 만들 수 있을거 같은데 귀찮아서 안함 */
    //    public static DateTime trimWith5min(DateTime dateTime){
    //        int min = dateTime.getMinuteOfHour();
    //        int remain = min % 5;
    //        return trim(dateTime.minusMinutes(remain),DateTimeFieldType.minuteOfHour());
    //    }
    //
    //    /**
    //     * 현재 기준으로 시분초를 맞춰준다.
    //     * 하루 한번 작동하는 간단 스케쥴링에 사용.
    //     *  */
    //    public static DateTime withAfterDay(int hh,int mm,int ss){
    //        DateTime time = new DateTime();
    //        time =  time.withHourOfDay(hh);
    //        time =  time.withMinuteOfHour(mm);
    //        time =  time.withSecondOfMinute(ss);
    //        time =  time.withMillisOfSecond(0);
    //        if(time.isBeforeNow()) time = time.plusDays(1); //이미 지났다면 하루 추가.
    //        return time;
    //    }
    //
    //    /** 매달 마지막 X요일 찾기(DateTimeConstasnts 참고) */
    //    public static LocalDate getNowMonthLastDay(int dateValue){
    //        DateTime now = new DateTime();
    //        LocalDate dt = new LocalDate().dayOfMonth().withMaximumValue().withDayOfWeek(dateValue);
    //        if(dt.getMonthOfYear() != now.getMonthOfYear()){
    //            dt = dt.minusDays(7);
    //        }
    //        return dt;
    //    }
    //
    //    /** 오늘이 가장 마지막 X요일과 같은지 ? JodaUtil.isEqualNowMonthLastDay(DateTimeConstants.FRIDAY) */
    //    public static Boolean isEqualNowMonthLastDay(int dateValue){
    //        LocalDate now = new LocalDate();
    //        LocalDate lastDay = getNowMonthLastDay(dateValue);
    //        return now.isEqual(lastDay);
    //    }
    //
    //    /** 지정한 날짜에서 하루 더한 뒤 1분만 빼준다(전시광고 등 히스토리 쪽에서 사용) */
    //    public static String plusOneDayMinusOneMin(LocalDate date){
    //        return TimeFormat.TIME2.get(date.plusDays(1).toDateTimeAtStartOfDay().minusMillis(1));
    //    }
}