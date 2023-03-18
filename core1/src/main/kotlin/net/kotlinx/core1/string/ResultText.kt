package net.kotlinx.core1.string

/** 텍스트로 된 결과 */
data class ResultText(
    val ok: Boolean,
    val result: String,
)