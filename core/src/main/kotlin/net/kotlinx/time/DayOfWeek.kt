package net.kotlinx.time

import java.time.LocalDate

/**
 * Joda DateTime의 DOW의 3자리 영문표현
 * Joda-Time uses the ISO standard Monday to Sunday week.
 * week index 1 (Monday) to 7 (Sunday)
 */
enum class DayOfWeek(
    /** new DateTime().getDayOfWeek() 하면 나오는 그거 */
    val dayOfWeek: Int,
    /** 1자리 한글이름  */
    val text: String
) {
    MON(1, "월"),
    TUE(2, "화"),
    WED(3, "수"),
    THU(4, "목"),
    FRI(5, "금"),
    SAT(6, "토"),
    SUN(7, "일"),
    ;

    val isWeekend: Boolean
        get() = this == SAT || this == SUN

    /**
     * 크론 표현식에서는 sun 부터 시작함 -> 1-7 또는 SUN-SAT
     * https://docs.aws.amazon.com/ko_kr/scheduler/latest/UserGuide/schedule-types.html#cron-based
     * */
    val cronIndex: Int = if (dayOfWeek == 7) 1 else dayOfWeek + 1

    companion object {

        fun from(index: Int): DayOfWeek {
            for (each in entries) {
                if (each.dayOfWeek == index) return each
            }
            throw IllegalArgumentException("index가 잘못되었습니다. $index")
        }

        fun fromCronIndex(index: Int): DayOfWeek {
            for (each in entries) {
                if (each.cronIndex == index) return each
            }
            throw IllegalArgumentException("index가 잘못되었습니다. $index")
        }

        fun from(text: String): DayOfWeek {
            return when (text) {
                "SUN", "SUNDAY" -> SUN
                "MON", "MONDAY" -> MON
                "TUE", "TUESDAY" -> TUE
                "WED", "WEDNESDAY" -> WED
                "THU", "THURSDAY" -> THU
                "FRI", "FRIDAY" -> FRI
                "SAT", "SATURDAY" -> SAT
                else -> throw IllegalArgumentException("text가 잘못되었습니다. $text")
            }
        }
    }
}

fun LocalDate.toDayOfWeek() = DayOfWeek.from(this.dayOfWeek.value)