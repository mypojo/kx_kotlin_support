package net.kotlinx.aws.logs

import aws.sdk.kotlin.services.cloudwatchlogs.CloudWatchLogsClient
import aws.sdk.kotlin.services.cloudwatchlogs.getQueryResults
import aws.sdk.kotlin.services.cloudwatchlogs.model.QueryStatus
import aws.sdk.kotlin.services.cloudwatchlogs.startQuery
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import net.kotlinx.exception.KnownException
import net.kotlinx.time.toLong
import java.time.LocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CloudWatchQuery {

    /** 검색할 로그 디렉토리 */
    lateinit var logGroupNames: List<String>

    /** 조회 쿼리 */
    lateinit var query: String

    /** 몇번까지 시도할지? */
    var repeat: Int = 100

    /** 페이지 리미트. 디폴트로 max */
    var limit: Int = 10000

    /** 쿼리 종료되었는지 체크를 시도하는 간격 */
    var checkInterval: Duration = 1.seconds

    /** 쿼리 체크 타임아웃 */
    var checkTimeout: Duration = 1.minutes

    var startTime: LocalDateTime = LocalDateTime.now().minusDays(1)
    var endTime: LocalDateTime = LocalDateTime.now()

}

data class CloudWatchResult(
    val logGroupName: String,
    val logStream: String,
    val message: String,
) {

    /** 시간 컨트롤은 일단 무시. UTC 조절해야함. */
    fun toLogLink(): String = CloudWatchUtil.toLogLink(logGroupName, logStream)

}

/**
 * 쿼리 날리고 받아온다.
 * 대부분 로컬에서 테스트로 작동함으로 그냥 여기다 간단히 코딩함
 *  */
suspend fun CloudWatchLogsClient.queryAndWait(block: CloudWatchQuery.() -> Unit = {}): List<CloudWatchResult> {
    val log = KotlinLogging.logger {}
    val op = CloudWatchQuery().apply(block)
    val startQueryResponse = this.startQuery {
        this.logGroupNames = op.logGroupNames
        this.queryString = "fields @log,@logStream,@message | filter @message like /${op.query}/ | limit ${op.limit}"
        this.startTime = op.startTime.toLong()
        this.endTime = op.endTime.toLong()
        this.limit = op.limit
    }

    delay(2.seconds) //기본 대기

    val client = this
    return withTimeout(op.checkTimeout) {
        repeat(op.repeat) { cnt ->
            val resp = client.getQueryResults {
                this.queryId = startQueryResponse.queryId
            }

            when (resp.status) {

                QueryStatus.Running, QueryStatus.Scheduled -> {
                    log.debug { " -> ${cnt + 1}/${op.repeat} ${resp.status}.." }
                }

                QueryStatus.Complete -> {
                    return@withTimeout resp.results!!.map { line ->
                        //성능 무시
                        CloudWatchResult(
                            line.first { it.field == "@log" }.value!!.substringAfter(":"), //이상하게 넘어온다. ex) 123456789:/aws/lambda/xx-fn
                            line.first { it.field == "@logStream" }.value!!,
                            line.first { it.field == "@message" }.value!!.dropLast(1) //마지막 문자로 들어오는 개행을 삭제해준다.
                        )
                    }
                }

                else -> throw IllegalStateException("${resp.status} is not required")
            }
            delay(op.checkInterval)

        }

        throw KnownException.StopException("반복횟수 초과")
    }

}