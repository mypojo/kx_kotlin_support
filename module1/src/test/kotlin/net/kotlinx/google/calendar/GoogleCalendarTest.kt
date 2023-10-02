package net.kotlinx.google.calendar

import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.EventDateTime
import mu.KotlinLogging
import net.kotlinx.google.GoogleSecret
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*


internal class GoogleCalendarTest {

    private val log = KotlinLogging.logger {}

    @Test
    fun `기본테스트`() {

        val secret = GoogleSecret {
            secretDir = File("C:\\Users\\mypoj\\.google/")
        }

        // Calendar API를 초기화합니다.
        val calendar = Calendar.Builder(secret.transport, secret.jsonFactory, secret.credential)
            .setApplicationName("My Calendar App")
            //.setScopes(Collections.singletonList(CalendarScopes.CALENDAR))
            .build()

        println(calendar.CalendarList().list())

        // 캘린더 ID를 설정합니다.
        val calendarId = "va5ki7q0uqcg13re1re23l2frg@group.calendar.google.com"

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
        println(eventResponse)

        // 이벤트 ID를 출력합니다.


//        val credential = GoogleCredential.fromStream(File("C:\\Users\\mypoj\\.aws\\xx.json").inputStream())
//
//        //val credentials = GoogleCredentials.getApplicationDefault()
//
//        // 캘린더 API 초기화
//        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
//        val jsonFactory = GsonFactory.getDefaultInstance()
//
//        // Initialize Calendar service with valid OAuth credentials
//        val service = Calendar.Builder(httpTransport, jsonFactory, credential)
//            .setApplicationName("applicationName").build()
//
//// Iterate through entries in calendar list
//
//// Iterate through entries in calendar list
//        var pageToken: String? = null
//        do {
//            val calendarList: CalendarList = service.calendarList().list().setPageToken(pageToken).execute()
//            val items: List<CalendarListEntry> = calendarList.getItems()
//            for (calendarListEntry in items) {
//                println(calendarListEntry.summary)
//            }
//            pageToken = calendarList.getNextPageToken()
//        } while (pageToken != null)


    }
}