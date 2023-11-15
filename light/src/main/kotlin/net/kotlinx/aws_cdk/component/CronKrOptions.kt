package net.kotlinx.aws_cdk.component

import software.amazon.awscdk.services.events.CronOptions
import software.amazon.awscdk.services.events.Schedule

/** 한국시간 입력기 */
@Deprecated("신제품 출시로 더이상 사용하지 않음")
class CronKrOptions {

    /** 한국 날짜 등록 */
    var krDay: Int? = null

    /** 한국시간 등록 */
    var krHour: Int? = null

    var year: String? = null
    var month: String? = null
    var weekDay: String? = null
    var hour: String? = null
    var day: String? = null
    var minute: String? = null


    fun toSchedule(): Schedule {
        return Schedule.cron(
            CronOptions.builder()
                .year(year)
                .month(month)
                .weekDay(weekDay)
                .hour(hour)
                .day(day)
                .minute(minute)
                .build()
        )
    }

    /** 한국 시간을 UTC로 변경해준다. */
    fun updateToUtc(offsetHour: Int = OFFSET_HOUR): CronKrOptions {
        if (krHour == null) return this

        val hourKr = krHour!! - offsetHour
        val isYesterday = hourKr < 0
        hour = "${hourKr + (if (isYesterday) 24 else 0)}" //시간은 24시간 더하고
        day = krDay?.let { "${it + (if (isYesterday) -1 else 0)}" } //날짜는 하루 당겨줘야함
        return this
    }

    //==================================================== 자주 사용함 ======================================================

    /** 한국시간 시분 인라인 입력 ex) 02:30  */
    var hhmm: String
        get() = throw UnsupportedOperationException()
        set(value) {
            val split = value.split(":")
            check(split.size == 2)
            krHour = split[0].toInt()
            minute = split[1]
        }

    /** 간편생성은 만들지 않음..바리에이션이 너무 많다. */
    companion object {
        /** 시차 */
        const val OFFSET_HOUR: Int = 9

        /** 특정 시간들을 UTC로 변환 */
        fun hourToUtc(vararg krHours: Int): String = krHours.map { CronKrOptions().apply { krHour = it }.updateToUtc().hour }.joinToString(",")
    }
}