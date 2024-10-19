package net.kotlinx.aws.firehose

import com.lectra.koson.obj
import net.kotlinx.aws.AwsClient
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.time.toYmd
import java.time.LocalDateTime

class KdfLoggerTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("KdfLogger") {
            val aws by koinLazy<AwsClient>()
            Then("로깅 테스트") {
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
    }
}
