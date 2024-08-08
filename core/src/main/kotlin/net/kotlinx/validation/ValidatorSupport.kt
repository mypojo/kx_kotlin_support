package net.kotlinx.validation

import net.kotlinx.validation.bean.ValidationResult
import net.kotlinx.validation.bean.ValidationResultException


/**
 * 하나의 성공메세지 or 다수의 실패 메세지가 존재할 수 있는 벨리데이터 정의 (공용)
 * 예외발생시 리스트에 추가. -> 리스트에 하나라도 추가되면 에러로 간주. 반대로 비어있으면 성공으로 간주
 * 리턴은 성공 메세지
 * */
typealias Validator = suspend (MutableList<String>) -> String


/**
 * 간단한 단건 벨리데이션 체크
 * 복잡한 and / or 조건이 있을경우 line 벨리데이션 사용할것
 *  */
fun valid(ok: Boolean, block: () -> ValidationResult) {
    if (ok) return

    block().let {
        throw ValidationResultException(listOf(it))
    }
}

/** 단순 메세지만 벨리데이션 체크 */
fun valid(code: String = "", ok: Boolean, lazyMessage: () -> Any) {
    valid(ok) {
        ValidationResult(
            code = code,
            fieldId = "",
            fieldName = "",
            message = lazyMessage().toString(),
        )
    }
}


