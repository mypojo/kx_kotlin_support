package net.kotlinx.aws.logs

import net.kotlinx.aws.AwsConfig
import net.kotlinx.core.string.encodeUrl
import net.kotlinx.core.time.toLong
import java.time.LocalDateTime

object CloudWatchUtil {

    /**
     * 클라우드와치 로그 링크를 리턴
     * 이 뒤에 필터를 붙일 수 있다. (별도의 특문 없이 그대로 + 하면됨)
     */
    fun toLogLink(logGroupName: String?, logStreamName: String?, region: String = AwsConfig.SEOUL): String {
        val escapedGroupName = (logGroupName ?: "").encodeUrl()
        val escapedStreamName = (logStreamName ?: "").encodeUrl()

        //AWS Lambda 스냅스타트의 경우 logStreamName 을 제공하지 앟아서
        return when (escapedStreamName) {
            "unknown" -> "https://$region.console.aws.amazon.com/cloudwatch/home?region=$region#logsV2:log-groups/log-group/$escapedGroupName"
            else -> "https://$region.console.aws.amazon.com/cloudwatch/home?region=$region#logsV2:log-groups/log-group/$escapedGroupName/log-events/$escapedStreamName"
        }
    }

    //==================================================== 시간 필터 ======================================================

    /**
     * 로그 링크시 시작과 종료시간 설정을 만들어준다.
     * 한국시간 잘 적용됨
     * ex) $3Fstart$3D 1648738800000 $26end$3D 1649775599000
     */
    fun toBetweenQuery(start: LocalDateTime?, end: LocalDateTime?): String = toStartQuery(start) + toEndQuery(end)
    fun toStartQuery(time: LocalDateTime?): String = if (time == null) "" else "$3Fstart$3D${time.toLong()}"
    fun toEndQuery(time: LocalDateTime?): String = if (time == null) "" else "$26end$3D${time.toLong()}"
}