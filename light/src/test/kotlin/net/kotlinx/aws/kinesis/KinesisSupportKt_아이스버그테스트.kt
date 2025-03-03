package net.kotlinx.aws.kinesis

import io.kotest.matchers.shouldBe
import net.kotlinx.aws.athena.AthenaExecute
import net.kotlinx.aws.firehose.*
import net.kotlinx.concurrent.delay
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.string.print
import net.kotlinx.time.TimeFormat
import net.kotlinx.time.truncatedMills
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

class KinesisSupportKt_아이스버그테스트 : BeSpecLight() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("기본 입력/조회/삭제") {

            val database = "d1"
            val name = "demo-test"
            val basicDate = TimeFormat.YMD.get()
            val tables = listOf("http_log", "http_log2")
            val ids = (1..3).toList()
            val streamName = "to_iceberg-dev"
            val now = LocalDateTime.now().truncatedMills()

            val demo = HttpLog(
                basicDate = basicDate,
                name = name,
                eventId = "test",
                metadata = "metadata",
                reqTime = now,
                reqUri = "test",
                reqMethod = "test",
                reqHeader = "test",
                reqBody = "body",
                respCode = 200,
                respBody = streamName
            )

            /** 10초 플러시 해도 실제 조회까지는 한참 걸림 */
            suspend fun waitForStream() = 60.seconds.delay()

            Then("입력") {
                val datas = tables.flatMap { tableName ->
                    val router = IcebergRouter(database, tableName)
                    ids.map { id ->
                        router.wrap(
                            demo.copy(
                                eventId = id.toString(),
                                metadata = "from-${tableName}"
                            )
                        )
                    }
                }
                aws97.firehose.putRecordBatch(streamName, datas)
                waitForStream()

                val lines = athenaModule97.readAll {
                    """
                        SELECT * FROM $database.${tables[0]} WHERE basic_date = '${basicDate}' and name = '${name}'
                        union all 
                        SELECT * FROM $database.${tables[1]}  WHERE basic_date = '${basicDate}' and name = '${name}'
                        limit 10;
                    """.trimIndent()
                }
                lines.print()
                lines.size shouldBe 6 + 1
            }

            Then("업데이트") {
                val tableName = tables[0]
                val id = ids[1]
                val updateText = "test-updated"
                val router = IcebergRouter(database, tableName,IcebergOperation.UPDATE)
                val json  =router.wrap(
                    demo.copy(
                        eventId = id.toString(),
                        metadata = "from-${tableName}",
                        reqMethod =  updateText,
                    )
                )
                aws97.firehose.putRecord(streamName, json)
                waitForStream()

                val lines = athenaModule97.readAll {
                    """
                        SELECT * FROM $database.${tableName} WHERE basic_date = '${basicDate}' and name = '${name}'
                    """.trimIndent()
                }
                lines.print()
                lines.size shouldBe 3 + 1
                lines.first { it[2] == "$id" }[6] == updateText
            }

            Then("삭제") {
                val tableName = tables[1]
                val id = ids[2]

                val router = IcebergRouter(database, tableName,IcebergOperation.DELETE)
                val json  =router.wrap(
                    demo.copy(
                        eventId = id.toString(),
                        metadata = "from-${tableName}",
                    )
                )
                aws97.firehose.putRecord(streamName, json)
                waitForStream()

                val lines = athenaModule97.readAll {
                    """
                        SELECT * FROM $database.${tableName} WHERE basic_date = '${basicDate}' and name = '${name}'
                    """.trimIndent()
                }
                lines.print()
                lines.size shouldBe 2 + 1
            }

            Then("테스트데이터 정리") {
                athenaModule97.startAndWait(
                    tables.map { "delete FROM $database.${it} WHERE basic_date = '${basicDate}' and name = '${name}' " }.map { AthenaExecute(it) }
                )
            }

        }
    }

}
