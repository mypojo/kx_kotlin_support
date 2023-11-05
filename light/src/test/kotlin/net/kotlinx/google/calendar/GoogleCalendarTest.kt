package net.kotlinx.google.calendar

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.s3.getObjectDownload
import net.kotlinx.aws.ssm.findAndWrite
import net.kotlinx.aws.toAwsClient1
import net.kotlinx.core.concurrent.sleep
import net.kotlinx.google.GoogleSecret
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

internal class GoogleCalendarTest {

    private val log = KotlinLogging.logger {}

    val aws = AwsConfig().toAwsClient1()

    val workDir = File("C:\\Users\\dev\\.google/").apply { mkdirs() }

    val secret = GoogleSecret {
        secretDir = workDir
        runBlocking {
            aws.ssm.findAndWrite("/google/app-access/oauth2_client", File(workDir, secretClientFileName))
            aws.s3.getObjectDownload("kotlinx", "store/secret/google/app-access/StoredCredential", File(workDir, GoogleSecret.SECRET_STORED_FILE_NAME))
        }
    }
    val calendar = GoogleCalendar(secret.createService())

    val calId = "va5ki7q0uqcg13re1re23l2frg@group.calendar.google.com"

    @Test
    fun `리스팅`() {
        calendar.list(calId)
    }

    @Test
    fun `기본테스트`() {

        val event = calendar.insert(calId) {
            title = "긴급작업5"
            desc = "사실 별거아님"
            date = LocalDate.now() to LocalDate.now()
            //startTime = LocalDateTime.now() to LocalDateTime.now().plusDays(3)
        }

        10.seconds.sleep()

        event.title = "작업 종료!!"
        calendar.update(calId, event)

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


    }
}