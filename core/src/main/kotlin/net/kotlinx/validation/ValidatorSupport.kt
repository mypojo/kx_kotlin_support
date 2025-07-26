package net.kotlinx.validation

import net.kotlinx.validation.bean.ValidationResult
import net.kotlinx.validation.bean.ValidationResultException


/**
 * 간단한 단건 벨리데이션 체크
 * ex) step02 사용자 입력값에 대한 필드 수준의 커스텀 벨리데이션
 * 복잡한 and / or 조건이 있을경우 이거대신 line 벨리데이션 사용할것
 *  */
fun valid(check: Boolean, block: () -> ValidationResult) {
    if (check) return

    block().let {
        throw ValidationResultException(listOf(it))
    }
}

/** 단순 메세지만 벨리데이션 체크 */
fun valid(code: String = "", check: Boolean, lazyMessage: () -> Any) {
    valid(check) {
        ValidationResult(
            code = code,
            fieldId = "",
            fieldName = "",
            message = lazyMessage().toString(),
        )
    }
}


