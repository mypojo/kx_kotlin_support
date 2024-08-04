package net.kotlinx.validation.conditional

/**
 * CoroutineConditionMatchResult 가 다 private 이다.. 왜지???
 * 이때문에 따로 만든다.
 *  */
data class LineConditionalResultLog(
    val condition: String,
    val matches: Boolean,
    /** 다수의 로그가 있을 수 있음 */
    val message: List<String>,
)