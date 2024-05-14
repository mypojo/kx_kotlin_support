package net.kotlinx.validation.konform

import io.konform.validation.ValidationBuilder
import io.konform.validation.jsonschema.minLength


//==================================================== 벨리데이션 한글 메세지. 참고만 해주세요 ======================================================

fun ValidationBuilder<String>.minLengthKr(length: Int) {
    minLength(length) hint "입력값 [{value}] -> ${length}자 이상을 입력해야 합니다"
}