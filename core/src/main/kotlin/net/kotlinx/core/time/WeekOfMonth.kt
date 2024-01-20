package net.kotlinx.core.time

import java.time.LocalDate

/**
 * 원래 X월의 1주차 같은 개념은 없다. 억지로 만든거임
 * 표준에 따라 한 주는 월~일
 * 1주차의 정의 : 해당 월의 첫 월요일부터 1주차
 * ex) 8월 1일이 화요일~일요일 이라면 8월 1일은 8월 1주차가 아니라 7월의 마지막 주차임
 *
 */
class WeekOfMonth(date: LocalDate = LocalDate.now()) {

    /** 월 */
    val month: Int

    /** 주차 */
    val weekOfMonth: Int

    /** 입력한 값이 속한 주의 월요일  */
    val startDate: LocalDate

    /** 입력한 값이 속한 주의 일요일  */
    val endDate: LocalDate

    init {
        val day = date.dayOfMonth //날짜
        val dow = date.dayOfWeek.value //4 & 7
        var value = day - dow
        var month = date.month
        if (value < 0) {
            month = month.minus(1) //한달 전 정보 사용
            value = day - dow + month.maxLength()
        }
        this.month = month.value
        weekOfMonth = value / 7 + 1
        startDate = date.minusDays((dow - 1).toLong())
        endDate = startDate.plusDays(6)

    }

    override fun toString(): String = "${month}월 ${weekOfMonth}주차 ($startDate ~ $endDate)"
}