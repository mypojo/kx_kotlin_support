package net.kotlinx.core.time

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
    MON(1, "월"), TUE(2, "화"), WED(3, "수"), THU(4, "목"), FRI(5, "금"), SAT(6, "토"), SUN(7, "일");

    val isWeekend: Boolean
        get() = this == SAT || this == SUN

    companion object {
        fun from(index: Int): DayOfWeek {
            for (each in values()) {
                if (each.dayOfWeek == index) return each
            }
            throw IllegalArgumentException("index가 잘못되었습니다. $index")
        }
    }
}

inline fun LocalDate.toDayOfWeek() = DayOfWeek.from(this.dayOfWeek.value)