package net.kotlinx.aws.logs

import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient
import net.kotlinx.core.gson.GsonData
import net.kotlinx.core.regex.RegexSet
import net.kotlinx.core.string.toLocalDate
import net.kotlinx.core.time.TimeListUtil
import net.kotlinx.core.time.toYmd
import net.kotlinx.test.TestLevel03
import net.kotlinx.test.TestRoot
import java.time.LocalTime

class CloudWatchLogsSupportKt_로그쿼리 : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient()

    @TestLevel03
    fun test() {

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
                val creationDataIds = doQuery.map { it.split(RegexSet.SPACE)[5].toLong() }.toSet()
                data.put(date.toYmd(), GsonData.fromObj(creationDataIds))
                log.info { " -> $date ${doQuery.size} -> ${creationDataIds.size}건" }
            }
            println(data)

        }

    }

}