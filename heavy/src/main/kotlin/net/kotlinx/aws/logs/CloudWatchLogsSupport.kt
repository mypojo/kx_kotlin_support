package net.kotlinx.aws.logs

import aws.sdk.kotlin.services.cloudwatchlogs.CloudWatchLogsClient
import aws.sdk.kotlin.services.cloudwatchlogs.deleteLogStream
import aws.sdk.kotlin.services.cloudwatchlogs.describeLogStreams
import kotlinx.coroutines.delay
import mu.KotlinLogging

/**
 * 로그 스트림을 다 삭제한다.
 * 테스트 할때 사용함. 로거 따로 없음
 * @param interval  100이면 exceed 오류남.
 */
suspend fun CloudWatchLogsClient.cleanLogStream(logGroupName: String, interval: Long = 150) {
    val log = KotlinLogging.logger {}
    for (i in 0..100) {
        val logStreams = this.describeLogStreams {
            this.logGroupName = logGroupName
        }.logStreams!!
        if (logStreams.isEmpty()) return

        delay(interval) // 여기서도 한번 쉬어야함

        log.debug { " -> deleteLogStream ${logStreams.size}건 삭제.. " }
        //Rate exceeded 가 빨리뜬다. 하나씩 지우자
        logStreams.forEach {
            this.deleteLogStream {
                this.logGroupName = logGroupName
                this.logStreamName = it.logStreamName!!
            }
            delay(interval)
        }
        if (logStreams.size < 50) return
    }
    log.info { "deleteLogStream completed" }
}

/**
 * 로그를 스캔한다.
 * 람다 snapstart에서 logStreamName 을 확인할 수 없기 때문에 검색 후 해당 위치로 이동하도록 조치함
 *  */
suspend fun CloudWatchLogsClient.cleanLogStreamasd(logGroupName: String, interval: Long = 200) {
    this.queryAndWait {

    }
}
