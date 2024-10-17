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


    /**
     * ECS 스팟 중단돤경우.
     * 람다나 SFN 등을 걸어서, 재시도 하게 해야함
     * https://docs.aws.amazon.com/ko_kr/AmazonECS/latest/developerguide/fargate-capacity-providers.html
     *  */
    val ECS_SPOT_FAIL = EventPattern.builder()
        .source(listOf("aws.ecs"))
        .detailType(listOf("ECS Task State Change"))
        .detail(
            mapOf(
                "stopCode" to listOf("SpotInterruption"),
            )
        )
        .build()!!

}