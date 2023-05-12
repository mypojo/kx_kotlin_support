package net.kotlinx.aws_cdk.component

/** 어플리케이션이 실행되는 대표적인 서비스들. 런타임 권한용에 붙일것 */
enum class CdkIamService(
    val iamName: String
) {

    /** ECS (AWS-BATCH) 구동용 */
    ECS_TASK("ecs-tasks"),

    /** 람다 구동용 */
    LAMBDA("lambda"),

    /** 글루 구동용 */
    GLUE("glue"),

    /** 코드파이프라인 구동용 */
    CODE_PIPELINE("codepipeline"),

    /** 코드 빌드 */
    CODE_BUILD("codebuild"),

    /** 이벤트브릿지 이벤트 (커밋 트리거로 파이프라인 실행 등) */
    EVENT("events"),

    /** SFN은 반쯤 어플리케이션으로 봐야할듯. S3,DDB 등 많은것을 다룸 */
    SFN("states"),
    ;

    val serviceName: String
        get() = "${iamName}.amazonaws.com"
}