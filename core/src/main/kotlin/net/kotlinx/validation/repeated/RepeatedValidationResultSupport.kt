package net.kotlinx.validation.repeated

import jakarta.validation.ValidationException
import net.kotlinx.string.toTextGridPrint

/** 정렬 */
fun List<RepeatedValidationResult>.sort(): List<RepeatedValidationResult> = this.sortedWith(compareBy({ it.config.group }, { it.config.code }))

/** 단순 확인용1 */
fun List<RepeatedValidationResult>.print1() {
    listOf("그룹", "코드", "메세지").toTextGridPrint {
        this.filter { !it.success }.flatMap { r -> r.msgs.map { arrayOf(r.config.group, r.config.code, it) } }
    }
}

/** 단순 확인용2 */
fun List<RepeatedValidationResult>.print2() {
    listOf("그룹", "코드", "설명", "담당자", "검사범위", "걸린시간", "결과", "메세지").toTextGridPrint {
        this.map { it.toGridArray() }
    }
}

/** 예외를 던져서 알려줌 */
fun List<RepeatedValidationResult>.andThrowIfInvalid(lineSeparator: String = "\n") {
    val invalidResults = this.filter { it.success.not() }
    if (invalidResults.isEmpty()) return

    this.print2()
    invalidResults.flatMap { it.msgs }.joinToString(lineSeparator) { "#$it" }.also { throw ValidationException(it) }
}