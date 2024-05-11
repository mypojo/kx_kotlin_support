package net.kotlinx.google.calendar

import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import net.kotlinx.time.toDate
import net.kotlinx.time.toYmdF01
import java.time.LocalDate
import java.time.LocalDateTime

class GoogleCalendarData(block: GoogleCalendarData.() -> Unit = {}) {

    /** 수정시 필수 */
    lateinit var eventId: String

    /** 제목 */
    lateinit var title: String

    /** 본문 */
    lateinit var desc: String

    /** 장소 */
    var location: String = "Seoul, Korea"

    /** 날짜 범위 (종일 일정) */
    var date: Pair<LocalDate, LocalDate>? = null

    /** 날짜시간 범위 */
    var dateTime: Pair<LocalDateTime, LocalDateTime>? = null

    init {
        block(this)
    }

    fun toEvent(): Event {
        // 이벤트를 생성합니다.
        val event = Event()
        event.summary = title
        event.description = desc
        event.location = location

        check(date != null || dateTime != null) { "date or datetime is required" }

        date?.let {
            event.start = EventDateTime().apply { date = com.google.api.client.util.DateTime(it.first.toYmdF01()) }
            event.end = EventDateTime().apply { date = com.google.api.client.util.DateTime(it.second.toYmdF01()) }
        }

        dateTime?.let {
            event.start = EventDateTime().apply { dateTime = com.google.api.client.util.DateTime(it.first.toDate()) }
            event.end = EventDateTime().apply { dateTime = com.google.api.client.util.DateTime(it.second.toDate()) }
        }

        return event
    }
}