package net.kotlinx.core.string

/**
 * 텍스트로 된 범용 결과 객체
 * @see Result
 *  */
data class ResultText(
    val ok: Boolean,
    val result: String,
)