package net.kotlinx.module.job.define

enum class JobScheduleType(
    /** 소팅용 */
    val order: Int,
) {


    /** AWS Eventbridge 등의 이벤트로 트리거 - 월 */
    MONTH(0),

    /** AWS Eventbridge 등의 이벤트로 트리거 - 일 */
    DAY(11),

    /** AWS Eventbridge 등의 이벤트로 트리거 - 시간 */
    HOUR(21),

    /** AWS Eventbridge 등의 이벤트로 트리거 - 분 */
    MINUTES(31),

    /** SQS 등의 이벤트로 트리거 */
    EVENT(51),

    /** AWS step function 으로 트리거 */
    SFN(51),

    /** 테스트 등 */
    NONE(99),
    ;
}