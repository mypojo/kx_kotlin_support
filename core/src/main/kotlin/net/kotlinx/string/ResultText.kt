package net.kotlinx.string

/**
 * 텍스트로 된 범용 결과 객체
 * 기본 Result -> 제너렉 하기 귀찮음
 * @see Result
 *  */
data class ResultText(
    val ok: Boolean,
    val result: String,
)