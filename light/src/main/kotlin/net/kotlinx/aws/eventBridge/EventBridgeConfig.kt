package net.kotlinx.aws.eventBridge

import net.kotlinx.aws.AwsConfig

data class EventBridgeConfig(
    /**
     * 이벤트 버스 명. ARN 생략시 현 계정의 이벤트버스로 전송
     * AWS CDK 참조
     *  */
    val eventBusName: String,

    /**
     * 이벤트 대분류
     * ex) aws.ecs
     * ex) ${pn}.job / ${pn}.web ..
     * */
    val source: String,

    /**
     * 이벤트 소분류
     * ex) ECS Task State Change
     * ex) jobFinally, webFilter ..
     *  */
    val detailType: String,

    /**
     * 이벤트 출처
     * ex) arn:aws:ecs:ap-northeast-2:112233:task/xxx-web_cluster-dev/saff2q341242
     * ex) 보통 안씀
     *  */
    val resources: List<String> = emptyList()
) {

    companion object {
        /**
         * 타계정의 eventBusName 로 보낼때.
         * */
        fun byAccount(awsId: String, eventBusName: String, region: String = AwsConfig.REGION_KR) = "arn:aws:events:${region}:${awsId}:event-bus/$eventBusName"
    }


}
