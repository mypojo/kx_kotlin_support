package net.kotlinx.google.calendar

import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.CalendarListEntry
import mu.KotlinLogging
import net.kotlinx.google.GoogleService

/**
 * 간단 캘린더 등록
 * https://developers.google.com/calendar/api/v3/reference?apix=true&hl=ko
 */
class GoogleCalendar(service: GoogleService) {

    private val log = KotlinLogging.logger {}

    val calendar = service.calendar

    /** 필요할때 완성하기 */
    fun listAll() {
        var pageToken: String? = null
        do {
            val calendarList: CalendarList = calendar.calendarList().list().setPageToken(pageToken).execute()
            val items: List<CalendarListEntry> = calendarList.items
            for (calendarListEntry in items) {
                log.debug { calendarListEntry.summary }
            }
            pageToken = calendarList.nextPageToken
        } while (pageToken != null)
//        repeatCollectUntil { keep, nextToken ->
//
//        }
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