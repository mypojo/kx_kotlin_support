package net.kotlinx.aws.firehose

import com.lectra.koson.arr
import com.lectra.koson.obj
import net.kotlinx.aws.athena.AthenaPartitionSqlBuilder
import net.kotlinx.aws.eventBridge.EventBridgeConfig
import net.kotlinx.aws.eventBridge.event
import net.kotlinx.aws.eventBridge.putEvents
import net.kotlinx.json.koson.toGsonData
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecHeavy
import net.kotlinx.time.TimeUtil
import java.time.LocalDateTime

class FirehoseLogger_eventbridgeTest : BeSpecHeavy() {

    init {
        initTest(KotestUtil.IGNORE)

        Given("FirehoseLogger") {

            val profile = findProfile97

            Then("파티셔닝") {
                val builder = AthenaPartitionSqlBuilder {
                    bucketName = "${profile}-work-dev"
                    prefix = "data/level1"
                }
                val dates = TimeUtil.between("20241105" to "20241107")
                val sources = listOf("job", "web")
                val datas = dates.flatMap {
                    sources.map { source ->
                        mapOf(
                            "basic_date" to it,
                            "source" to "${profile}.${source}",
                        )
                    }
                }
                val addSqls = builder.generateAddSqlBatch("eventbridge", datas)
                addSqls.forEach { athenaModule97.execute(it) }
            }

            Then("파이어호스에 직접 입력") {
                val obj = obj {
                    "source" to "my.test"
                    "detail-type" to "김ABC"
                    "resources" to arr["AAA", "BBB"]
                    "detail" to obj {
                        "name" to "김ABC"
                        "age" to 20
                        "now" to LocalDateTime.now()
                    }
                }
                aws97.firehose.putRecord("eventbridge-dev", obj.toString())
            }

            Then("이벤트브릿지를 거쳐서 입력") {

                val config = EventBridgeConfig(
                    "${profile}-dev",
                    "${profile}.job",
                    "-",
                    listOf("AAA", "xxx.job"),
                )
                val obj = obj {
                    "name" to "이벤트브릿지 테스트 v3"
                    "now" to LocalDateTime.now()
                }
                aws97.event.putEvents(config, listOf(obj.toGsonData().toString()))

            }

        }
    }
}
