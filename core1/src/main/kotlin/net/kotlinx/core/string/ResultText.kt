package net.kotlinx.core.string

/** 텍스트로 된 범용 결과 객체 */
data class ResultText(
    val ok: Boolean,
    val result: String,
)