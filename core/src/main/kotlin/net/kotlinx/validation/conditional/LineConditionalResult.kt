package net.kotlinx.validation.conditional

import com.linecorp.conditional.kotlin.CoroutineConditionContext
import net.kotlinx.validation.bean.ValidationResult
import net.kotlinx.validation.bean.ValidationResultException

data class LineConditionalResult(
    /** context */
    val ctx: CoroutineConditionContext,
    /** 전체 성공 여부. */
    val ok: Boolean,
    /** 상세 로그 결과 */
    val logs: List<LineConditionalResultLog>,
) {

    /**
     * 실패한경우 예외 던짐
     * @see net.kotlinx.validation.bean.throwIfFail
     *  */
    fun throwIfFail() {
        if (ok) return

        val fails = logs.filter { !it.matches }.flatMap { logs ->
            logs.messages.map {
                ValidationResult(
                    code = logs.condition,
                    fieldId = "",
                    fieldName = "",
                    message = it
                )
            }
        }
        throw ValidationResultException(fails)

    }
}
