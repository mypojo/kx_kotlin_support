package net.kotlinx.aws.athena.table.cloudtrail

import net.kotlinx.aws.athena.table.AthenaTable
import net.kotlinx.kotest.KotestUtil
import net.kotlinx.kotest.initTest
import net.kotlinx.kotest.modules.BeSpecLight
import net.kotlinx.number.padStart
import net.kotlinx.time.TimeUtil

class CloudtrailToolTest : BeSpecLight() {

    private val profileName by lazy { findProfile97 }

    init {
        initTest(KotestUtil.PROJECT)

        Given("최초 생성") {

            val awsConfig = athenaModule97.aws.awsConfig
            val athenaTable = AthenaTable {
                tableName = "cloudtrail_logs"
                database = "d1"
                bucket = "${profileName}-audit-prod"
                s3Key = "cloudtrail/AWSLogs/${awsConfig.awsId}/CloudTrail/${awsConfig.region}/"
            }

            val tool = CloudtrailTool {
                athenaModule = athenaModule97
                table = athenaTable
            }

            Then("스키마 생성됨") {
                tool.dropTable()
                tool.create()
            }
            Then("파티션 단일 생성") {
                tool.updatePartition(
                    listOf(
                        Triple("2024", "09", "01")
                    )
                )
            }
            Then("파티션 전체 생성") {
                val dates = TimeUtil.betweenToLocalDate("20240801" to "20240910")
                val triples = dates.map { Triple(it.year.toString(), it.monthValue.padStart(2), it.dayOfMonth.padStart(2)) }
                tool.updatePartition(triples)
            }
        }

    }

}
