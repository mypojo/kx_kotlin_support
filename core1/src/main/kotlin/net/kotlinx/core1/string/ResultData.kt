package net.kotlinx.core1.string

/** 범용 결과 객체 */
data class ResultData(
    val ok: Boolean,
    val data: Any,
)