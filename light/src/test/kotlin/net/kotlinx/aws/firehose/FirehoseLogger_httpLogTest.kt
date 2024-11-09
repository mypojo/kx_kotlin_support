package net.kotlinx.aws.firehose

import com.lectra.koson.obj
import net.kotlinx.aws.athena.AthenaPartitionSqlBuilder
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.string.print
import net.kotlinx.time.TimeFormat
import net.kotlinx.time.toYmd
import java.time.LocalDateTime

class FirehoseLogger_httpLogTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("FirehoseLogger") {

            Then("파티셔닝") {
                val builder = AthenaPartitionSqlBuilder {
                    bucketName = "sin-data-dev"
                    prefix = "data"
                }

                val datas = listOf(
                    mapOf(
                        "basic_date" to TimeFormat.YMD.get(),
                        "name" to "test",
                    )
                )
                val addSqls = builder.generateAddSqlBatch("http_log-dev", datas)
                println(addSqls)
            }

            Then("HTTP 로깅 테스트") {

                val logger = FirehoseLogger {
                    firehose = aws97.firehose
                    streamName = "http_log-dev"
                }
                for (i in 0..6) {
                    val resp = obj {
                        "id" to "#$i"
                        "value" to "datas?"
                        "이름" to "영감님"
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

            Then("HTTP 로깅 쿼리 테스트") {
                athenaModule97.readAll {
                    """
                        SELECT event_id,req_time AT TIME ZONE 'Asia/Seoul' req_time,metadata,req_uri, req_method,req_header, req_body, resp_code, resp_body, basic_date, name
                        FROM http_log
                        order by req_time desc
                        limit 10
                    """.trimIndent()
                }.print()
            }

        }
    }
}
