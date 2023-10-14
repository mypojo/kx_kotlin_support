package net.kotlinx.notion

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.s3.getObjectDownload
import net.kotlinx.aws.ssm.find
import net.kotlinx.aws.ssm.findAndWrite
import net.kotlinx.aws.toAwsClient1
import net.kotlinx.core.test.TestRoot
import net.kotlinx.google.GoogleSecret
import net.kotlinx.google.calendar.GoogleCalendar
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import java.io.File

class NotionDatabaseToGoogleCalendarTest : TestRoot() {

    val aws = AwsConfig().toAwsClient1()
    val secretValue by lazy {
        runBlocking {
            aws.ssm.find("/notion/key")!!
        }
    }
    val client = OkHttpClient()

    val workDir = File("C:\\Users\\dev\\.google/").apply { mkdirs() }
    val secret = GoogleSecret {
        secretDir = workDir
        runBlocking {
            aws.ssm.findAndWrite("/google/app-access/oauth2_client", File(workDir, secretClientFileName))
            aws.s3.getObjectDownload("kotlinx", "store/secret/google/app-access/StoredCredential", File(workDir, GoogleSecret.SECRET_STORED_FILE_NAME))
        }
    }

    @Test
    fun test() {
        val synch = NotionDatabaseToGoogleCalendar {
            notionDatabaseClient = NotionDatabaseClient(client, secretValue)
            notionPageBlockClient = NotionPageBlockClient(client, secretValue)
            notionDbId = "48741c1766314c14938901047680703d"
            notionPageId = "4b18e3f52ce84487b64acab8ab2b5837"
            title = "이벤트명"
            desc = "내용상세"
            date = "날짜"
            type = "구분"

            googleCalendar = GoogleCalendar(secret.createService())
            calendarDefaultId = "b8291e41a4d3ddd3e8c91eca770464aa6f90b32a33e561b4e0c30b1fd22232b7@group.calendar.google.com"
            calendarTypeIdMap = mapOf(
                "회사" to "va5ki7q0uqcg13re1re23l2frg@group.calendar.google.com",
                "이벤트" to "cjmvo8554i4rmm2sq8utbokvbs@group.calendar.google.com",
            )
        }

        runBlocking {
            synch.updateOrInsert()
        }

    }

}