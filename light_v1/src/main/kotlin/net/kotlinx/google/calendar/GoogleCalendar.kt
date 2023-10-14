package net.kotlinx.google.calendar

import com.google.api.services.calendar.model.Event
import mu.KotlinLogging
import net.kotlinx.google.GoogleService

/**
 * 간단 캘린더 등록
 * https://developers.google.com/calendar/api/v3/reference?apix=true&hl=ko
 */
class GoogleCalendar(service: GoogleService) {

    private val log = KotlinLogging.logger {}

    val calendar = service.calendar

    /**
     * 보통 스냅스타트용
     * @param maxResultCnt 디폴트 250. 최대 2500 개인듯
     * */
    fun list(calendarId: String, maxResultCnt: Int = 250): List<Event> {
        val resp = calendar.events().list(calendarId).setMaxResults(maxResultCnt).execute()
        return resp.items
    }

    fun insert(calendarId: String, block: GoogleCalendarData.() -> Unit): GoogleCalendarData = insert(calendarId, GoogleCalendarData().apply(block))

    fun insert(calendarId: String, data: GoogleCalendarData): GoogleCalendarData {
        val execute = calendar.events().insert(calendarId, data.toEvent()).execute()!!
        data.eventId = execute.id
        return data
    }

    fun update(calendarId: String, data: GoogleCalendarData): GoogleCalendarData {
        calendar.events().update(calendarId, data.eventId, data.toEvent()).execute()
        return data
    }


}