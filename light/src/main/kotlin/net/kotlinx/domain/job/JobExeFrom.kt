package net.kotlinx.domain.job

/**
 * 잡이 호출된 경로
 */
enum class JobExeFrom {

    /** 모름 */
    UNKNOWN,

    /** LOCAL 등에서 관리자의 강제 실행  */
    ADMIN,

    /** 웹서버 등에서 사용자 이벤트로  */
    WEB,

    /** 주기적으로 실행되는 배치  */
    CRON,

    /** sqs, sql 등으로 자료가 있으면 실행  */
    QUEUE,

    /** SFN 마키용 잡 */
    SFN,

    ;


}