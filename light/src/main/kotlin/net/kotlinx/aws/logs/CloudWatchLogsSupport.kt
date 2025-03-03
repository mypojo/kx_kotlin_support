package net.kotlinx.aws.logs

import aws.sdk.kotlin.services.cloudwatchlogs.CloudWatchLogsClient
import aws.sdk.kotlin.services.cloudwatchlogs.deleteLogStream
import aws.sdk.kotlin.services.cloudwatchlogs.describeLogStreams
import aws.sdk.kotlin.services.cloudwatchlogs.getLogEvents
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist
import net.kotlinx.collection.repeatUntil
import net.kotlinx.number.padStart
import net.kotlinx.number.toLocalDateTime
import net.kotlinx.time.toKr01
import java.io.File

val AwsClient.logs: CloudWatchLogsClient
    get() = getOrCreateClient { CloudWatchLogsClient { awsConfig.build(this) }.regist(awsConfig) }

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
        //if (logStreams.size < 50) return  // 50건 이하가 리턴될 수 있다
    }
    log.info { "deleteLogStream completed" }
}

/**
 * 간단 텍스트 다운로드
 * 최대 X회만큼 반복한 다음 다운로드 한다.
 * @param repeatCnt  1회당 약 1mb 라고 보면 됨
 * */
suspend fun CloudWatchLogsClient.download(logGroupName: String, logStreamName: String, out: File, repeatCnt: Int = 10) {
    val log = KotlinLogging.logger {}
    check(!out.exists()) { "이미 존재하는 파일입니다 : $out" }
    var token: String? = null
    repeatUntil(repeatCnt) {
        val results = this.getLogEvents {
            this.logGroupName = logGroupName
            this.logStreamName = logStreamName
            this.startFromHead = true  //디폴트는 뒤에서 읽게 되어있음. 여기서는 앞에서 부터 써준다.
            this.nextToken = token
        }
        token = results.nextForwardToken
        val events = results.events!!
        out.appendText(events.joinToString("\n") { it.message!! }) //성능 주의!! 많이 하지 말것
        log.trace { " -> [$it] 로그수 ${events.size} 저장됨" }
        events.isEmpty()
    }
}

suspend fun CloudWatchLogsClient.getLogAnomaly() {
    //아직 지원하지 않는거 같음
}

