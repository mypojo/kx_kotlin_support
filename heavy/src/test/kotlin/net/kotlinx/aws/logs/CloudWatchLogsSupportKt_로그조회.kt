package net.kotlinx.aws.logs

import aws.sdk.kotlin.services.cloudwatchlogs.getLogEvents
import kotlinx.coroutines.runBlocking
import net.kotlinx.aws.AwsConfig
import net.kotlinx.aws.toAwsClient
import net.kotlinx.core.time.toLong
import net.kotlinx.test.TestLevel03
import net.kotlinx.test.TestRoot
import java.time.LocalDateTime

class CloudWatchLogsSupportKt_로그조회 : TestRoot() {

    val aws = AwsConfig(profileName = "sin").toAwsClient()

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

}