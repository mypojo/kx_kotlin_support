package net.kotlinx.aws.logs

import aws.sdk.kotlin.services.cloudwatchlogs.getLogEvents
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.regex.RegexSet
import net.kotlinx.core.string.toLocalDate
import net.kotlinx.core.string.toTextGrid
import net.kotlinx.core.time.TimeListUtil
import net.kotlinx.core.time.toLong
import net.kotlinx.core.time.toYmd
import net.kotlinx.kotest.BeSpecLog
import net.kotlinx.test.TestLevel03
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.LocalTime

class CloudWatchLogsSupportKt_로그삭제 : BeSpecLog(){
    init {
        val projectName = "sin"

        val aws = AwsConfig(projectName).toAwsClient()

        @TestLevel03
        fun test() {

            runBlocking {
                aws.logs.cleanLogStream("/aws/lambda/${projectName}-fn-dev")
            }

        }

        @TestLevel03
        fun 단일조회() = runBlocking {
            val logs = aws.logs.getLogEvents {
                this.logGroupName = "/aws/ecs/web-prod"
                this.logStreamName = "sin-web/sin-web_container-prod/31d271dc4ae147c4b570317a60252ae7"
                this.limit = 10
                this.startTime = LocalDateTime.now().minusHours(16).toLong()  //디폴트 한국시간임
                this.endTime = LocalDateTime.now().minusHours(1).toLong()
            }.events!!
            logs.forEach {
                println(" => ${it.message}")
            }
            println(logs.size)
        }

        @Test
        fun check() {

            runBlocking {
                val doQuery = aws.logs.queryAndWait {
                    this.logGroupNames = listOf("/aws/lambda/aa/-fn-dev")
                    this.query = "WAS lambda 과금"
                    this.startTime = LocalDateTime.now().minusHours(1)
                }
                listOf("메시지", "링크").toTextGrid(doQuery.map { arrayOf(it.message, it.toLogLink()) }).print()
            }

        }

        fun test22() {

            runBlocking {

                //val dates = TimeListUtil.toList("20230501".toLocalDate(), "20230531".toLocalDate())
                val dates = TimeListUtil.toList("20230501".toLocalDate(), "20230501".toLocalDate())

                val data = GsonData.obj()
                dates.forEach { date ->
                    val doQuery = aws.logs.queryAndWait {
                        this.logGroupNames = listOf("/aws/lambda/sin-controller-prod")
                        this.query = "이미지 해시 오류"
                        this.startTime = date.atStartOfDay()
                        this.endTime = date.atTime(LocalTime.MAX)
                    }
                    val creationDataIds = doQuery.map { it.message.split(RegexSet.SPACE)[5].toLong() }.toSet()
                    data.put(date.toYmd(), GsonData.fromObj(creationDataIds))
                    log.info { " -> $date ${doQuery.size} -> ${creationDataIds.size}건" }
                }
                println(data)

            }

        }
    }
}