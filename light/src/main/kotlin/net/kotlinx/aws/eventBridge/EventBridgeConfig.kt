package net.kotlinx.aws.eventBridge

data class EventBridgeConfig (
    /**
     * 이벤트 버스 명
     * AWS CDK 참조
     *  */
    val eventBusName: String,

    /**
     * 이벤트 대분류
     * ex) aws.ecs
     * ex) samsung-01
     * */
    val source: String,

    /**
     * 이벤트 소분류
     * ex) ECS Task State Change
     * ex) job or web
     *  */
    val detailType: String,

    /**
     * 이벤트 출처
     * ex) arn:aws:ecs:ap-northeast-2:112233:task/xxx-web_cluster-dev/saff2q341242
     * ex) 보통 안씀
     *  */
    val resources: List<String> = emptyList()
)
