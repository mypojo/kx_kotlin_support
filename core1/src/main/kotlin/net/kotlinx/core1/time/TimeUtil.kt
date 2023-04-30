package net.kotlinx.core1.time

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors

/**
 * 모르는건 기본 API 먼저 확인할것
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

    /**
     * UTC ISO Instant 로 포매팅 (-9시간)
     * 벤더 디폴으로 파싱할때 사용된다.
     * athena 조회시에는 AT TIME ZONE 'Asia/Seoul' 를 사용
     * ex) kdf to parquet 변환시 (존 포함이나 오프셋 포함을 읽지 못함)  https://docs.aws.amazon.com/firehose/latest/dev/record-format-conversion.html
     * */
    val ISO_INSTANT = DateTimeFormatter.ISO_INSTANT.withZone(SEOUL)!!

    /** 디폴트 타임존 고정  */
    fun initTimeZone(timeZone: TimeZone = ZONE_SEOUL) {
        TimeZone.setDefault(timeZone)
    }

    /**
     * 서울 기준 밀리초 반환
     * System.currentTimeMillis() 와 동일하다. (확인할것)
     */
    @JvmStatic
    fun getMills(time: LocalDateTime): Long {
        return time.atZone(SEOUL).toInstant().toEpochMilli()
    }

    /** 밀리초 구하는게 짜증나졌다.  */
    @JvmStatic
    fun interval(start: LocalDateTime, end: LocalDateTime): Long {
        val endTime = getMills(end)
        val startTime = getMills(start)
        return endTime - startTime
    }

    fun toLocalDateTime(date: Date): LocalDateTime {
        return toLocalDateTime(date.time)
    }

    fun toLocalDateTime(millis: Long): LocalDateTime {
        val instant = Instant.ofEpochMilli(millis)
        return toLocalDateTime(instant)
    }

    fun toLocalDateTime(instant: Instant): LocalDateTime {
        return instant.atZone(SEOUL).toLocalDateTime()
    }

    fun toDate(localDateTime: LocalDateTime): Date {
        return Date.from(localDateTime.atZone(SEOUL).toInstant())
    }
    //==================================================== MONTH ======================================================
    /** 샘플용 간단 메소드  */
    fun toListMonth(format: TimeFormat, start: String?, end: String): List<String> {
        val dates = toList(format.toLocalDateTime(start), Period.ofMonths(1), format.toLocalDateTime(end))
        return dates.stream().map { dateTime: LocalDateTime? -> format[dateTime] }.collect(Collectors.toList())
    }

    /** 샘플용 간단 메소드  */
    fun toListMonth(start: String?, end: String): List<String> {
        return toListMonth(TimeFormat.YM, start, end)
    }
    //==================================================== DATE ======================================================
    /**
     * 시작부터 size만큼 field를 증가시켜 리스트로 반환한다.
     * ex) List<LocalDateTime> days = TimeUtil.toList(LocalDateTime.now(), Period.ofDays(1), 5);
     * 시작 시간을 포함한다.
    </LocalDateTime> */
    fun toList(start: LocalDate, size: Int): List<LocalDate> {
        val list: MutableList<LocalDate> = ArrayList()
        var current = start
        for (i in 0 until size) {
            list.add(current)
            current = current.plusDays(1)
        }
        return list
    }

    /** 같은 이름의 다른 버전  */
    fun toList(start: LocalDate, end: LocalDate?): List<LocalDate> {
        val list: MutableList<LocalDate> = ArrayList()
        var current = start
        while (!current.isAfter(end)) {
            list.add(current)
            current = current.plusDays(1)
        }
        return list
    }

    /** 샘플용 간단 메소드  */
    fun toListDate(start: String?, end: String): List<String> {
        val format = TimeFormat.YMD
        val localDates = toList(format.toLocalDate(start), format.toLocalDate(end))
        return localDates.stream().map { dateTime: LocalDate -> format[dateTime] }.collect(Collectors.toList())
    }
    //==================================================== TIME ======================================================
    /**
     * 시작부터 size만큼 field를 증가시켜 리스트로 반환한다.
     * ex) List<LocalDateTime> days = TimeUtil.toList(LocalDateTime.now(), Period.ofDays(1), 5);
     * 시작 시간을 포함한다.
    </LocalDateTime> */
    fun toList(start: LocalDateTime, period: Period?, size: Int): List<LocalDateTime> {
        val list: MutableList<LocalDateTime> = ArrayList()
        var current = start
        for (i in 0 until size) {
            list.add(current)
            current = current.plus(period)
        }
        return list
    }

    /**
     * 같은 이름의 다른 버전
     * 이게 베이스!
     */
    fun toList(start: LocalDateTime, period: Period?, end: LocalDateTime?): List<LocalDateTime> {
        val list: MutableList<LocalDateTime> = ArrayList()
        var current = start
        while (!current.isAfter(end)) {
            list.add(current)
            current = current.plus(period)
        }
        return list
    }
    //==================================================== 소스코드 마킹용 ======================================================
    /** 날짜의 첫 시간대를 리턴함  */
    @JvmOverloads
    fun trim(localDate: LocalDate = LocalDate.now()): LocalDateTime {
        return localDate.atTime(LocalTime.MIN)
    }
    //==================================================== 텍스트 파싱 ======================================================
    /**
     * 디폴트 파싱
     * ex) 2022-01-20T14:15:27.394237800
     */
    fun parseTime(time: String?): LocalDateTime {
        return LocalDateTime.parse(time)
    }

    /**
     * 타임 존이 없는 UTC 베이스의 Instance 반환용
     * ex) 2021-10-07T01:40:53Z
     * 네이버가 이렇게 준다.
     * 타임존이 필요하면 instant.atZone(TimeUtil.SEOUL) 이렇게 변환할것.
     */
    fun parseTimeUtc(time: String?): Instant {
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

    fun toIsoInstant(time: ZonedDateTime?): String {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(time)
        //return DateTimeFormatter.ISO_ZONED_DATE_TIME.format(time); //Zone이 들어가면 aws가 못읽는다 주의!.
    } //
    //    /** 동일시간 포함, period 간격 만큼의 DateTime을 리턴한다.
    //     * ex) List<DateTime> times = between(start,end,Period.days(1));
    //     * 년월일의 경우 시작,종료 일자가 포함된다.  */
    //    public static List<DateTime> between(BaseDateTime start,BaseDateTime end,ReadablePeriod period){
    //        Preconditions.checkArgument(start.isBefore(end) || start.isEqual(end),"start는 end보다 작거나 같아야 합니다");
    //        DateTime startTime = start.toDateTime();
    //        List<DateTime> times = Lists.newArrayList();
    //
    //        while(startTime.isBefore(end) || startTime.isEqual(end)){
    //            times.add(startTime);
    //            startTime = startTime.plus(period);
    //        }
    //        return times;
    //    }
    //
    //    /** 배열을 간단하게 포매팅 할때 사용된다.
    //     * ex) List<String> dates =  FluentIterable.from(times).transform(formatFuction(JodaUtil.YMD)).toList(); */
    //    public static Function<BaseDateTime,String> formatFuction(final DateTimeFormatter formatter){
    //        return new Function<BaseDateTime,String>(){
    //            @Override
    //            public String apply(BaseDateTime arg0) {
    //                return arg0.toString(formatter);
    //            }
    //        };
    //    }
    //
    //    /** 위를 이용한 샘플  .toSet() 등을 하길 바람 */
    //    public static List<String> betweenDate(String startDate,String endDate){
    //        Preconditions.checkState(!Strings.isNullOrEmpty(startDate),"startDate is required");
    //        Preconditions.checkState(!Strings.isNullOrEmpty(endDate),"endDate is required");
    //        LocalDate start = JodaUtil.toLocalDate(startDate);
    //        LocalDate end = JodaUtil.toLocalDate(endDate);
    //        //크기 비교는 안함
    //        List<DateTime> between = JodaUtil.between(start.toDateTimeAtCurrentTime(), end.toDateTimeAtCurrentTime(), Period.days(1));
    //        //return FluentIterable.from(between).transform(formatFuction(JodaUtil.YMD)). toList(); //다운그레이드
    //        return FluentIterable.from(between).transform(formatFuction(JodaUtil.YMD)).toList();
    //    }
    //
    //    public static List<String> betweenMonth(String startMonth,String endMonth){
    //        Preconditions.checkState(!Strings.isNullOrEmpty(startMonth),"startMonth is required");
    //        Preconditions.checkState(!Strings.isNullOrEmpty(endMonth),"endMonth is required");
    //        LocalDate start = JodaUtil.toLocalMonth(startMonth);
    //        LocalDate end = JodaUtil.toLocalMonth(endMonth);
    //        List<DateTime> between = JodaUtil.between(start.toDateTimeAtCurrentTime(), end.toDateTimeAtCurrentTime(), Period.months(1));
    //        return FluentIterable.from(between).transform(formatFuction(JodaUtil.YM)).toList();
    //    }
    //
    //    /** 시작 - 종료일을 포함해서 30일을 리턴한다.  */
    //    public static List<String> betweenDate(String endDate,int days){
    //        String startDate = TimeFormat.YMD.plusDays(endDate, -days+1);
    //        return betweenDate(startDate, endDate);
    //    }
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