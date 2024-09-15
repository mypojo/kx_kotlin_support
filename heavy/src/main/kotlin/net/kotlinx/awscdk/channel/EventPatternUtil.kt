package net.kotlinx.awscdk.channel

import software.amazon.awscdk.services.events.EventPattern


object EventPatternUtil {

    /** AWS에서 관심있게 봐야할 이벤트들 */
    val AWS_CORE = EventPattern.builder()
        .source(
            listOf(
                "aws.ecs",
                "aws.sns",
                "aws.codecommit",
                "aws.autoscaling",
            )
        ).build()!!


    /** ECS 헬스체크 실패 */
    val ECS_HEALTH_FAIL = EventPattern.builder()
        .source(listOf("aws.ecs"))
        .detailType(listOf("ECS Task State Change"))
        .detail(
            mapOf(
                "lastStatus" to listOf("STOPPED"),
                "stoppedReason" to mapOf(
                    "prefix" to listOf("Task failed ELB health checks")
                ),
            )
        )
        .build()!!


}