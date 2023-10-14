package net.kotlinx.google

import com.google.api.services.calendar.Calendar
import com.google.api.services.sheets.v4.Sheets

/**
 * 구글 서비스 캐시
 */
class GoogleService(secret: GoogleSecret) {

    /** 구글 시트 */
    val sheets: Sheets by lazy { Sheets.Builder(secret.transport, secret.jsonFactory, secret.credential).setApplicationName(secret.applicationName).build() }

    /** 구글 캘린더  */
    val calendar: Calendar by lazy { Calendar.Builder(secret.transport, secret.jsonFactory, secret.credential).setApplicationName(secret.applicationName).build() }

}