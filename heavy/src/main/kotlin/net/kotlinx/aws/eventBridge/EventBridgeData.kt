package net.kotlinx.aws.eventBridge

@Deprecated("안씀")
interface EventBridgeData {

    /**
     * 이벤트 버스 명
     * AWS CDK 참조
     *  */
    val eventBusName: String

    /**
     * 이벤트 대분류
     * ex) aws.ecs
     * ex) samsung-01
     * */
    val source: String

    /**
     * 이벤트 소분류
     * ex) ECS Task State Change
     * ex) job or web
     *  */
    val detailType: String

    /**
     * 이벤트 출처
     * ex) arn:aws:ecs:ap-northeast-2:112233:task/xxx-web_cluster-dev/saff2q341242
     * ex) null로 해도 됨
     *  */
    val resources: List<String>
    
    /** 상제 json 문자열 */
    val detail: String
}
