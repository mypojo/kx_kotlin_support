package net.kotlinx.aws.kinesis

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import net.kotlinx.aws.AwsInstanceType
import net.kotlinx.aws.athena.AthenaExecute
import net.kotlinx.aws.firehose.*
import net.kotlinx.aws.firehose.logData.LogData
import net.kotlinx.concurrent.delay
import net.kotlinx.json.gson.json
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.print
import net.kotlinx.time.TimeFormat
import net.kotlinx.time.truncatedMills
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Duration.Companion.seconds

class KinesisSupportKt_log_data_테스트 : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("기본 입력/조회/삭제") {

            val aws = aws49

            val streamName = "to_iceberg-dev"
            val database = "ds"
            val tableName = "log_data"
            val projectName = "${findProfile49}-test"
            val basicDate = TimeFormat.YMD.get()
            val ids = (1..3).map { it.toString() }
            val now = LocalDateTime.now().truncatedMills()
            val router = IcebergJsonBuilder(database, tableName)

            val demo = LogData(
                basicDate = basicDate,
                projectName = projectName,
                eventDiv = "API",
                eventId = UUID.randomUUID().toString(),
                eventTime = now,
                instanceType = AwsInstanceType.LOCAL,
                eventName = "/aa/bb/cc",
                eventDesc = "대매뉴 -> 중메뉴 -> 소메뉴",
                eventStatus = "200",
                eventMills = 400L,
                metadata = json {
                    "clientIp" to "111.222.333.444"
                    "loginId" to "admin"
                },
                memberId = "999",
                g1 = "service",
                g2 = "login",
                g3 = "",
                keyword = "",
                x = json {
                    "input" to "test-input"
                },
                y = json {
                    "output" to "test-output"
                },
            )

            /** 최소 60초 딜레이 */
            suspend fun waitForStream() = (60 * 2).seconds.delay()

            Then("입력") {
                val datas = ids.map { id ->
                    router.build(
                        demo.copy(
                            eventId = id,
                        )
                    )
                }
                aws.firehose.putRecordBatch(streamName, datas)
                waitForStream()

                val lines = athenaModule49.readAll {
                    """
                        SELECT * FROM $database.${tableName} WHERE basic_date = '${basicDate}' and project_name = '${projectName}'
                        limit 10;
                    """.trimIndent()
                }
                lines.print()
                lines.size shouldBe 3 + 1
            }

            Then("업데이트") {
                val id = ids[1]
                val updateText = "FAIL"
                val json = router.build(demo.copy(eventId = id, eventStatus = updateText), IcebergOperation.UPDATE)
                aws.firehose.putRecord(streamName, json)
                waitForStream()

                val lines = athenaModule49.readAll {
                    """
                        SELECT * FROM $database.${tableName} WHERE basic_date = '${basicDate}' and project_name = '${projectName}'
                    """.trimIndent()
                }
                lines.print()
                lines.size shouldBe 3 + 1 //헤더 포함
                lines.map { it.joinToString("\t") }.joinToString("\n") shouldContain updateText
            }

            Then("삭제") {
                val id = ids[2]

                val json = router.build(demo.copy(eventId = id), IcebergOperation.DELETE)
                aws.firehose.putRecord(streamName, json)
                waitForStream()

                val lines = athenaModule49.readAll {
                    """
                        SELECT * FROM $database.${tableName} WHERE basic_date = '${basicDate}' and project_name = '${projectName}'
                    """.trimIndent()
                }
                lines.print()
                lines.size shouldBe 2 + 1
            }

            Then("테스트데이터 정리") {
                athenaModule49.startAndWait(
                    AthenaExecute("delete FROM $database.${tableName} WHERE basic_date = '${basicDate}' and project_name = '${projectName}' ")
                )
            }

        }
    }

}