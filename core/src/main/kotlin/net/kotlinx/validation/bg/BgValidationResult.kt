package net.kotlinx.validation.bg

import jakarta.validation.ValidationException
import net.kotlinx.string.abbr
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.time.toTimeString

data class BgValidationResult(
    val config: BgValidation,
    val duration: Long,
    val success: Boolean,
    val msgs: List<String>,
) {
    fun toGridArray(): Array<*> {
        return arrayOf(
            config.group,
            config.code,
            config.desc.joinToString(",").abbr(60),
            config.authors.joinToString(",") { it.id },
            config.range,
            duration.toTimeString(),
            success,
            msgs.joinToString(",").abbr(60),
        )
    }
}

/** 정렬 */
fun List<BgValidationResult>.sort(): List<BgValidationResult> = this.sortedWith(compareBy({ it.config.group }, { it.config.code }))

/** 단순 확인용1 */
fun List<BgValidationResult>.print1() {
    listOf("그룹", "코드", "메세지").toTextGridPrint {
        this.filter { !it.success }.flatMap { r -> r.msgs.map { arrayOf(r.config.group, r.config.code, it) } }
    }
}

/** 단순 확인용2 */
fun List<BgValidationResult>.print2() {
    listOf("그룹", "코드", "설명", "담당자", "검사범위", "걸린시간", "결과", "메세지").toTextGridPrint {
        this.map { it.toGridArray() }
    }
}

/** 예외를 던져서 알려줌 */
fun List<BgValidationResult>.andThrowIfInvalid(lineSeparator: String = "\n") {
    val invalidResults = this.filter { it.success.not() }
    if (invalidResults.isEmpty()) return

    this.print2()
    invalidResults.flatMap { it.msgs }.joinToString(lineSeparator) { "#$it" }.also { throw ValidationException(it) }
}