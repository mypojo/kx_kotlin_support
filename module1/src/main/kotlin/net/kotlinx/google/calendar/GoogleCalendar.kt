package net.kotlinx.google.calendar

import com.google.api.services.calendar.model.EventDateTime
import net.kotlinx.google.GoogleService
import java.util.*

/**
 * 간단 캘린더 등록
 */
class GoogleCalendar(service: GoogleService, val calendarId: String) {

    val calendar = service.calendar

    fun addEvent() {
        // 이벤트를 생성합니다.
        val event = com.google.api.services.calendar.model.Event()
        event.setSummary("My New Event 22")
        event.setDescription("This is my new event.")
        event.setLocation("Seoul, Korea")
        event.setStart(
            EventDateTime().setDateTime(
                com.google.api.client.util.DateTime(Date())
            )
        )
        event.setEnd(
            EventDateTime().setDateTime(
                com.google.api.client.util.DateTime(Date())
//                LocalDateTime.of(2023, 10, 2, 11, 0, 0),
//                ZoneId.of("Asia/Seoul")
            )
        )

        // 이벤트를 추가합니다.
        val eventResponse = try {
            calendar.events().insert(calendarId, event).execute()
        } catch (e: Exception) {
            println(e.message)
        }
    }


}