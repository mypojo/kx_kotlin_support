package net.kotlinx.string

/**
 * 범용 결과 객체
 * 기본 Result -> 제너렉 하기 귀찮음
 *  */
data class ResultData(
    val ok: Boolean,
    val data: Any,
)