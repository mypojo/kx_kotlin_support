package net.kotlinx.module.job.define

enum class JobScheduleType(
    /** 소팅용 */
    val order: Int,
) {
    MONTH(0),
    DAY(11),
    HOUR(21),
    MINUTES(31),

    /** SQS 등의 이벤트로 트리거 */
    EVENT(51),

    /** AWS step function 으로 트리거 */
    SFN(51),

    /** 테스트 등 */
    NONE(99),
    ;
}