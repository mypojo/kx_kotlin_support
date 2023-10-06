package net.kotlinx.google.calendar

import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.CalendarListEntry
import net.kotlinx.google.GoogleService

/**
 * 간단 캘린더 등록
 * https://developers.google.com/calendar/api/v3/reference?apix=true&hl=ko
 */
class GoogleCalendar(service: GoogleService, val calendarId: String) {

    val calendar = service.calendar

    /** 필요할때 완성하기 */
    fun listAll() {
        var pageToken: String? = null
        do {
            val calendarList: CalendarList = calendar.calendarList().list().setPageToken(pageToken).execute()
            val items: List<CalendarListEntry> = calendarList.items
            for (calendarListEntry in items) {
                println(calendarListEntry.summary)
            }
            pageToken = calendarList.nextPageToken
        } while (pageToken != null)
//        repeatCollectUntil { keep, nextToken ->
//
//        }
    }

    fun insert(block: GoogleCalendarData.() -> Unit): GoogleCalendarData = insert(GoogleCalendarData().apply(block))

    fun insert(data: GoogleCalendarData): GoogleCalendarData {
        val execute = calendar.events().insert(calendarId, data.toEvent()).execute()!!
        data.id = execute.id
        return data
    }

    fun update(data: GoogleCalendarData): GoogleCalendarData {
        calendar.events().update(calendarId, data.id, data.toEvent()).execute()
        return data
    }


}