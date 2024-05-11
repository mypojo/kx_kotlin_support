package net.kotlinx.google.calendar

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.s3.getObjectDownload
import net.kotlinx.aws.ssm.findAndWrite
import net.kotlinx.aws.toAwsClient1
import net.kotlinx.concurrent.delay
import net.kotlinx.file.slash
import net.kotlinx.google.GoogleSecret
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.system.ResourceHolder
import java.io.File
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

internal class GoogleCalendarTest : BeSpecLog() {

    init {
        initTest(KotestUtil.SLOW)

        Given("GoogleCalendar") {

            val aws = AwsConfig().toAwsClient1()

            val workDir = ResourceHolder.USER_ROOT.slash(".google")

            val secret = GoogleSecret {
                secretDir = workDir
                runBlocking {
                    aws.ssm.findAndWrite("/google/app-access/oauth2_client", File(workDir, secretClientFileName))
                    aws.s3.getObjectDownload("kotlinx", "store/secret/google/app-access/StoredCredential", File(workDir, GoogleSecret.SECRET_STORED_FILE_NAME))
                }
            }
            val calendar = GoogleCalendar(secret.createService())
            val calId = "dj1cr2gi7tshqd13ltgca2p0ns@group.calendar.google.com"

            Then("리스팅") {
                val events = calendar.list(calId)
                events.take(4).forEach {
                    log.info { "${it.start.date} -> ${it.summary}" }
                }
            }

            xThen("이벤트 생성 / 수정 -> 쓰레기 데이터 생성되서 실행안함..삭제를 만들어야해") {
                val event = calendar.insert(calId) {
                    title = "긴급작업5"
                    desc = "사실 별거아님"
                    date = LocalDate.now() to LocalDate.now()
                }

                3.seconds.delay()
                event.title = "작업 종료!!"
                calendar.update(calId, event)
            }
        }
    }
}