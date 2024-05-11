package net.kotlinx.string

/** 범용 결과 객체 */
data class ResultData(
    val ok: Boolean,
    val data: Any,
)