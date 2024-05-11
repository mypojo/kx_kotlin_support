package net.kotlinx.domain.job

/**
 * 잡이 호출된 경로
 */
enum class JobExeFrom {

    /** 모름 */
    UNKNOWN,

    /** 관리자의 강제 실행  */
    ADMIN,

    /** AWS SFN  */
    SFN,

    /** WAS가 기동될때 필수적으로 한번 기동되는 배치  */
    INIT,

    /** 주기적으로 실행되는 배치  */
    CRON,

    /** sqs, sql 등으로 자료가 있으면 실행  */
    QUEUE,
    ;


}