package net.kotlinx.awscdk.channel

import software.amazon.awscdk.services.events.EventPattern


object EventPatternUtil {

    /**
     * 자주 사용되는거 2개만
     * ex) source: ["project"] / detailType: ["web"],
     *  */
    fun fromSource(source: List<String>, detailType: List<String> = emptyList()): EventPattern = EventPattern.builder().source(source).detailType(detailType).build()


    /** AWS에서 관심있게 봐야할 이벤트들 */
    val AWS_CORE = fromSource(
        listOf(
            "aws.ecs",
            "aws.sns",
            "aws.codecommit",
            "aws.autoscaling",
        )
    )

    /** ECS 헬스체크 실패 */
    val ECS_HEALTH_FAIL = EventPattern.builder()
        .source(listOf("aws.ecs"))
        .detailType(listOf("ECS Task State Change"))
        .detail(
            mapOf(
                "lastStatus" to listOf("RUNNING"),
                "stoppedReason" to mapOf(
                    "prefix" to "Task failed ELB health checks"
                ),
            )
        )
        .build()!!


}