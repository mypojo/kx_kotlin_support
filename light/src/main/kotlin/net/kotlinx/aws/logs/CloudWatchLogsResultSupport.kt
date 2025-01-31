package net.kotlinx.aws.logs

import aws.sdk.kotlin.services.cloudwatchlogs.model.GetQueryResultsResponse
import aws.sdk.kotlin.services.cloudwatchlogs.model.ResultField
import net.kotlinx.string.toLocalDateTime
import net.kotlinx.time.toZone
import java.time.LocalDateTime


typealias CloudWatchResult = List<ResultField>


/** 네이밍 마킹용 */
val GetQueryResultsResponse.logs: List<CloudWatchResult>
    get() = this.results!!


/** /이상하게 넘어온다. ex) 123456789:/aws/lambda/xx-fn */
val CloudWatchResult.logGroupName: String
    get() = this.first { it.field == "@log" }.value!!.substringAfter(":")

val CloudWatchResult.logStream: String
    get() = this.first { it.field == "@logStream" }.value!!

/** 마지막 문자로 들어오는 개행을 삭제해준다. */
val CloudWatchResult.message: String
    get() = this.first { it.field == "@message" }.value!!.dropLast(1)

/** 한국시간으로 변경해준다 */
val CloudWatchResult.timestamp: LocalDateTime
    get() = this.first { it.field == "@timestamp" }.value!!.toLocalDateTime().toZone()

val CloudWatchResult.logLink: String
    get() = CloudWatchUtil.toLogLink(logGroupName, logStream)
