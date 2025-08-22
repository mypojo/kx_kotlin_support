package net.kotlinx.aws.schedule

import net.kotlinx.core.Kdsl
import net.kotlinx.core.VibeCoding
import net.kotlinx.time.DayOfWeek
import java.time.LocalDateTime


/**
 * 요일과 시간을 기반으로 크론 표현식을 생성하는 클래스
 */
class CronExpression {

    @Kdsl
    constructor(block: CronExpression.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 입력설정 ======================================================

    /** 시간 목록 (0-23) */
    var configHours: List<Int>? = null

    /** 요일 목록 (null이면 매일 실행) */
    var configDaysOfWeek: List<DayOfWeek>? = null

    //==================================================== 기본값 ======================================================

    /** 년도 (고정) */
    var year = "*"

    /** 기본으로 매월의 모든일 */
    var dayOfMonth = "?"

    /** 기본으로 모든 월 */
    var month = "*"

    /** 기본으로 모든 요일 */
    var dayOfWeek = "*"

    /** 시간대 */
    var hour = "*"

    /** 분 (고정) */
    var minute = "0"

    /**
     * 크론 표현식 생성
     * @return 크론 표현식 문자열
     */
    override fun toString(): String {
        configHours?.let { hourList ->
            hourList.forEach { hour ->
                require(hour in 0..23) { "시간은 0-23 사이의 값이어야 합니다. 입력값: $hour" }
            }
            hour = hourList.sorted().joinToString(",")
        }
        configDaysOfWeek?.let {
            dayOfWeek = configDaysOfWeek!!.joinToString(",")
        }
        // 크론 표현식 형식: 분 시 일 월 요일 년
        return "$minute $hour $dayOfMonth $month $dayOfWeek $year"
    }

    companion object {

        /** 특정 시간으로 1회 지정 */
        fun from(time: LocalDateTime): CronExpression = CronExpression {
            minute = time.minute.toString()
            hour = time.hour.toString()
            dayOfMonth = time.dayOfMonth.toString()
            dayOfWeek = "?"
            month = time.month.toString()
            year = time.year.toString()
        }

        /**
         * 크론 표현식을 파싱하여 CronExpression 객체를 생성
         * @param cronExpression 크론 표현식 문자열 (예: "0 9,18 ? * MON,TUE,WED,THU,FRI *")
         * @return 파싱된 CronExpression 객체
         */
        @VibeCoding
        fun parse(cronExpression: String): CronExpression {
            val parts = cronExpression.trim().split("\\s+".toRegex())
            require(parts.size == 6) { "크론 표현식은 6개의 부분으로 구성되어야 합니다: 분 시 일 월 요일 년" }

            return CronExpression {
                minute = parts[0]
                hour = parts[1]
                dayOfMonth = parts[2]
                month = parts[3]
                dayOfWeek = parts[4]
                year = parts[5]

                // hour 부분을 configHours로 파싱
                if (parts[1] != "*") {
                    try {
                        configHours = parts[1].split(",").map { hourStr ->
                            val hourValue = hourStr.trim().toInt()
                            require(hourValue in 0..23) { "시간은 0-23 사이의 값이어야 합니다. 입력값: $hourValue" }
                            hourValue
                        }
                    } catch (e: NumberFormatException) {
                        // 숫자가 아닌 경우 (예: 범위 표현 등) configHours는 null로 유지
                    }
                }

                // dayOfWeek 부분을 configDaysOfWeek로 파싱
                if (parts[4] != "*" && parts[4] != "?") {
                    try {
                        val dayStrings = parts[4].split(",")
                        val dayList = mutableListOf<DayOfWeek>()

                        dayStrings.forEach { dayStr ->
                            val trimmedDay = dayStr.trim().uppercase()

                            when {
                                // 범위 표현 처리 (예: 2-6, MON-FRI)
                                trimmedDay.contains("-") -> {
                                    val rangeParts = trimmedDay.split("-")
                                    if (rangeParts.size == 2) {
                                        val startDay = parseSingleDay(rangeParts[0].trim())
                                        val endDay = parseSingleDay(rangeParts[1].trim())

                                        if (startDay != null && endDay != null) {
                                            // DayOfWeek의 dayOfWeek 값을 기준으로 범위 생성
                                            val startNum = startDay.dayOfWeek
                                            val endNum = endDay.dayOfWeek

                                            // 주의: 일요일(7)이 포함된 경우 처리
                                            if (startNum <= endNum) {
                                                for (i in startNum..endNum) {
                                                    DayOfWeek.from(i)?.let { dayList.add(it) }
                                                }
                                            } else {
                                                // 주말을 포함한 범위 (예: 6-1 = 토,일,월)
                                                for (i in startNum..7) {
                                                    DayOfWeek.from(i)?.let { dayList.add(it) }
                                                }
                                                for (i in 1..endNum) {
                                                    DayOfWeek.from(i)?.let { dayList.add(it) }
                                                }
                                            }
                                        }
                                    }
                                }
                                // 단일 값 처리
                                else -> {
                                    parseSingleDay(trimmedDay)?.let { dayList.add(it) }
                                }
                            }
                        }

                        configDaysOfWeek = dayList.distinct().takeIf { it.isNotEmpty() }
                    } catch (e: Exception) {
                        // 파싱 실패시 configDaysOfWeek는 null로 유지
                    }
                }
            }
        }

        /**
         * 단일 요일 문자열을 DayOfWeek로 파싱
         */
        private fun parseSingleDay(dayStr: String): DayOfWeek? {
            return when {
                dayStr.matches("\\d+".toRegex()) -> {
                    // 숫자 형태 (1=MON, 2=TUE, ..., 7=SUN - ISO 표준)
                    val dayNum = dayStr.toInt()
                    DayOfWeek.fromCronIndex(dayNum)
                }

                else -> DayOfWeek.from(dayStr)
            }
        }
    }
}