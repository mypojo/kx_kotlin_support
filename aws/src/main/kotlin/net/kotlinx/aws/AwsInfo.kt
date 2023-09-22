package net.kotlinx.aws

import net.kotlinx.aws.cloudWatch.CloudWatchUtil
import java.time.LocalDateTime


data class AwsInfo(
    /** 인스턴스 타입 */
    val instanceType: AwsInstanceType,
    /** 인스턴스 이름 (job id 등등) */
    val instanceName: String,
    /** 로그 그룹  */
    var logGroupName: String,
    /** 스트림 (API를 호출해야 알 수 있는것도 있음)  */
    var logStreamName: String,

    ) {

    /**
     * 클라우드와치 로그 링크를 리턴
     * 이 뒤에 필터를 붙일 수 있다.
     */
    fun toLogLink(startTime: LocalDateTime? = null): String {
        if (!instanceType.isLogLinkAble()) return "instanceType (${instanceType}) -> loglink is not available"
        val basicLink = CloudWatchUtil.toLogLink(logGroupName, logStreamName)
        val filter = when {
            !instanceType.isLogLinkWithTime() -> ""
            startTime == null -> CloudWatchUtil.toStartQuery(LocalDateTime.now().minusMinutes(5)) //람다나 ECS처럼 로그를 공유하는경우 로그가 많을 수 있으니 5분전 시작
            else -> CloudWatchUtil.toStartQuery(startTime)
        }
        return basicLink + filter
    }

}