package net.kotlinx.awscdk.iam

/**
 * 어플리케이션이 실행되는 대표적인 서비스들. 런타임 권한용에 붙일것
 * ## 그외 ##
 *  cloudtrail - 클라우드 트레일
 *  logdelivery.elasticloadbalancing ALB 로깅
 *  */
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

    /** 스케쥴링 기능 */
    SCHEDULER("scheduler"),

    /**
     * 레이크포메이션.
     * 레이크포메이션으로 변경시 어플리케이션 권한(app-admin)에도 있어야 하고  로그인 한 역할(DEV)에도 이게 필요함!
     *  */
    LAKEFORMATION("lakeformation"),

    /**
     * 퀵사이트.
     * 디폴트 역할은 athena-워크스페이스 primary 미선택시 오류남.
     * 개별 설정하기 귀찮아서 이걸로 통일
     * */
    QUICKSIGHT("quicksight"),
    ;

    val serviceName: String
        get() = "${iamName}.amazonaws.com"
}