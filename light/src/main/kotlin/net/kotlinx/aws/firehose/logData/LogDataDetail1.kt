package net.kotlinx.aws.firehose.logData

import net.kotlinx.aws.AwsInstanceType
import java.time.LocalDateTime


/**
 * 로그 데이터 개별 상세
 * @see LogData 필드 설명 참고
 *  */
data class LogDataDetail1(
    var basicDate: String,
    var projectName: String,
    var eventDiv: String,
    var eventId: String,
    var eventTime: LocalDateTime,
    var instanceType: AwsInstanceType
)
