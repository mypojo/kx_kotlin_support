package net.kotlinx.core.time

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.util.stream.Collectors

/**
 * 모르는건 기본 API 먼저 확인할것
 */
object TimeListUtil {


    //==================================================== MONTH ======================================================
    /** 샘플용 간단 메소드  */
    fun toListMonth(format: TimeFormat, start: String, end: String): List<String> {
        val dates = toList(format.toLocalDateTime(start), Period.ofMonths(1), format.toLocalDateTime(end))
        return dates.stream().map { dateTime: LocalDateTime? -> format[dateTime] }.collect(Collectors.toList())
    }

    /** 샘플용 간단 메소드  */
    fun toListMonth(start: String, end: String): List<String> {
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
    fun toListDate(start: String, end: String): List<String> {
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
    fun toList(start: LocalDateTime, period: Period, size: Int): List<LocalDateTime> {
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
    fun toList(start: LocalDateTime, period: Period, end: LocalDateTime?): List<LocalDateTime> {
        val list: MutableList<LocalDateTime> = ArrayList()
        var current = start
        while (!current.isAfter(end)) {
            list.add(current)
            current = current.plus(period)
        }
        return list
    }

}