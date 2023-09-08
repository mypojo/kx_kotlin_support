package net.kotlinx.aws.logs

import aws.sdk.kotlin.services.cloudwatchlogs.CloudWatchLogsClient
import aws.sdk.kotlin.services.cloudwatchlogs.deleteLogStream
import aws.sdk.kotlin.services.cloudwatchlogs.describeLogStreams
import kotlinx.coroutines.delay

/**
 * 로그 스르팀을 다 삭제한다.
 * 테스트 할때 사용함. 로거 따로 없음
 */
suspend fun CloudWatchLogsClient.cleanLogStream(logGroupName: String) {
    repeat(100) {
        val logStreams = this.describeLogStreams {
            this.logGroupName = logGroupName
        }.logStreams!!
        if (logStreams.isEmpty()) return@repeat
        delay(500) // 여기서도 한번 쉬어야함

        println(" -> deleteLogStream ${logStreams.size} .. ")
        //Rate exceeded 가 빨리뜬다. 하나씩 지우자
        logStreams.forEach {
            this.deleteLogStream {
                this.logGroupName = logGroupName
                this.logStreamName = it.logStreamName!!
            }
            delay(300) //  100이면 exceed 오류남. 넉넉하게 200 -> 300
        }
        if (logStreams.size < 50) return@repeat
    }
    println("deleteLogStream completed")
}
