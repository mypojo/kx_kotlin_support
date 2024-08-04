package net.kotlinx.validation.conditional

import jakarta.validation.ValidationException

/**
 * 상세 벨리데이션 예외를 추가한 케이스
 * 기본 메세지가 있긴 하지만 별도로 컨버팅 한 후 400 json 으로 리턴하는것이 좋다
 *    */
class LineConditionalResultException(val violationResults: Collection<LineConditionalResultLog>) : ValidationException(
    violationResults.joinToString("|") { it.message.joinToString("|") }
)