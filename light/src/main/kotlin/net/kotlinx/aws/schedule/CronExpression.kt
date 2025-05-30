package net.kotlinx.aws.schedule

import net.kotlinx.core.Kdsl
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
    }

}