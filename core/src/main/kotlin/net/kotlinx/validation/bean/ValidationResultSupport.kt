package net.kotlinx.validation.bean

import net.kotlinx.string.toTextGrid


/** 간단 출력 */
fun Collection<ValidationResult>.printSimple() {
    this.map {
        arrayOf(
            it.fieldId,
            it.fieldName,
            it.code,
            it.attr,
            it.invalidValue,
            it.message,
        )
    }.also {
        listOf("필드ID", "필드 명(한글)", "코드(어노테이션명)", "어노테이션 속성", "사용자입력값", "최종결과메세지").toTextGrid(it).print()
    }
}

/**
 * 실패한경우 예외 던짐
 * 이름 맞춤
 * @see net.kotlinx.validation.conditional.LineConditionalResult.throwIfFail
 *  */
fun Collection<ValidationResult>.throwIfFail() {
    if (this.isEmpty()) return
    throw ValidationResultException(this)
}