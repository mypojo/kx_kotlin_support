package net.kotlinx.aws1

import com.lectra.koson.obj
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.firehose.HttpLog
import net.kotlinx.aws.firehose.KdfLogger
import net.kotlinx.aws.toAwsClient1
import net.kotlinx.core.time.toYmd
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class AwsClient1_firehost_httpLog {

    val aws = AwsConfig(profileName = "sin").toAwsClient1()

    @Test
    fun `로거테스트`() = runBlocking {
        val logger = KdfLogger(aws.firehose, "http_log-dev")
        for (i in 0..6) {
            val resp = obj {
                "id" to "#$i"
                "value" to "datas"
            }
            val now = LocalDateTime.now()
            val httpLog = HttpLog(
                basicDate = now.toLocalDate().minusDays(1).toYmd(),
                name = "test",
                eventId = "",
                reqTime = now,
                reqUri = "https://aa.com",
                reqMethod = "post",
                respCode = 200,
                respBody = resp.toString()
            )
            logger.putRecord(httpLog)
        }
    }


}