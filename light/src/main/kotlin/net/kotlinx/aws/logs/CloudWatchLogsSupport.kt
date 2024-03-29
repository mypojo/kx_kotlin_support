package net.kotlinx.aws.logs

import aws.sdk.kotlin.services.cloudwatchlogs.CloudWatchLogsClient
import aws.sdk.kotlin.services.cloudwatchlogs.deleteLogStream
import aws.sdk.kotlin.services.cloudwatchlogs.describeLogStreams
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.kotlinx.core.number.padStart
import net.kotlinx.core.number.toLocalDateTime
import net.kotlinx.core.time.toKr01

/**
 * 로그 스트림을 다 삭제한다.
 * 테스트 할때 사용함. 로거 따로 없음
 * @param interval  150이면 오래 돌릴경우 exceed 오류남.
 */
suspend fun CloudWatchLogsClient.cleanLogStream(logGroupName: String, interval: Long = 150) {
    val log = KotlinLogging.logger {}
    for (i in 0..500) {
        val logStreams = this.describeLogStreams {
            this.logGroupName = logGroupName
        }.logStreams!!
        if (logStreams.isEmpty()) return

        delay(interval) // 여기서도 한번 쉬어야함

        log.debug { " -> ${i.padStart(3)} deleteLogStream ${logStreams.size}건 삭제..  삭제중인 로그 시간대 : ${logStreams.last().creationTime!!.toLocalDateTime().toKr01()} " }
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