package net.kotlinx.validation.repeated

enum class RepeatedValidationType {

    /** 지금기준 = 전체 = 실시간 */
    RUNTIME,

    /** 특정 기준일 데이터만 */
    DAY,
}