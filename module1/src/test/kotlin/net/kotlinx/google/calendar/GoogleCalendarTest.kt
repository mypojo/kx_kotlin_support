package net.kotlinx.google.calendar

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.CalendarListEntry
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import java.io.File


internal class GoogleCalendarTest {

    private val log = KotlinLogging.logger {}

    @Test
    fun `기본테스트`() {

        val credential = GoogleCredential.fromStream(File("C:\\Users\\mypoj\\.aws\\xx.json").inputStream())

        //val credentials = GoogleCredentials.getApplicationDefault()

        // 캘린더 API 초기화
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()

        // Initialize Calendar service with valid OAuth credentials
        val service = Calendar.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("applicationName").build()

// Iterate through entries in calendar list

// Iterate through entries in calendar list
        var pageToken: String? = null
        do {
            val calendarList: CalendarList = service.calendarList().list().setPageToken(pageToken).execute()
            val items: List<CalendarListEntry> = calendarList.getItems()
            for (calendarListEntry in items) {
                println(calendarListEntry.summary)
            }
            pageToken = calendarList.getNextPageToken()
        } while (pageToken != null)


    }
}