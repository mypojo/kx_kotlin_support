package net.kotlinx.core2.validation

import net.kotlinx.core1.collection.MutableListString
import net.kotlinx.core1.number.toTimeString
import net.kotlinx.core1.string.TextGrid
import net.kotlinx.core1.string.abbr
import net.kotlinx.core1.string.toTextGrid
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.validation.ValidationException

enum class ValidationRange {
    /** 지금기준 = 전체 = 실시간 */
    NOW,

    /** 특정 기준일 데이터만 */
    DAY,
}

/**
 * 벨리데이션 설정
 * ex) DB 무결성 체크, 실행환경 검사, 데이터 정합성&오차 검사 등등
 *
 * 요구사항
 * 1. 다수의 위반 발견시, 개별 오류 메시지로 나타나야함
 * 2. 위반 발견이나 예외가 발상하더라도 모든 로직은 작동해야함
 * 3. 설정과 최종 결과를 모아서 확인 가능해야함
 *  */
data class ValidationConfig(
    /** 코드 */
    var code: String = "",
    /** 그룹 */
    var group: String = "",
    /** 설명 (멀티라인보다 구분된 라인이 더 보기 좋아서 선택) */
    var descs: List<String> = emptyList(),
    /** 구현 설명 ex)  DB와 Athena를 비교 */
    var codeDesc: String = "",
    /** 벨리데이션 대상 범위. 매번 전체를 하면 너무 느림 */
    var range: ValidationRange = ValidationRange.NOW,
    /** 담당자 ID */
    var authors: Collection<String> = emptySet(),
    /** 벨리데이션 소스코드. 예외발생시 리스트에 추가. 리턴은 전체 서머리 성공 메세지 */
    var validationCode: (errs: MutableList<String>) -> String = { "ok" },
) {
    /** desc 설정 DSL */
    fun descs(block: MutableListString.() -> Unit) {
        descs = MutableListString().apply(block).toList()
    }

    /** descs의 인라인 버전 */
    var desc: String
        set(value) {
            descs = listOf(value)
        }
        get() = if (descs.size == 1) descs[0] else descs.joinToString(",")
}

/** ValidationConfig 설정 DSL */
inline fun validationConfig(block: ValidationConfig.() -> Unit): ValidationConfig = ValidationConfig().apply(block)

data class ValidationResult(
    val config: ValidationConfig,
    val duration: Long,
    val success: Boolean,
    val msgs: List<String>,
) {
    fun toGridArray(): Array<*> {
        return arrayOf(
            config.group,
            config.code,
            config.desc.abbr(60),
            config.codeDesc,
            config.authors,
            config.range,
            duration.toTimeString(),
            success,
            msgs.joinToString(",").abbr(60),
        )
    }
}

/** 스래드풀로 전환. */
fun List<ValidationConfig>.check(threadCnt: Int = Runtime.getRuntime().availableProcessors()): List<ValidationResult> {

    val workerPool: ExecutorService = Executors.newFixedThreadPool(threadCnt)

    val futures = this.map {
        val callable: () -> ValidationResult = submit@{
            val start = System.currentTimeMillis()
            try {
                val errs = mutableListOf<String>()
                val msg = it.validationCode(errs)
                val interval = System.currentTimeMillis() - start
                return@submit when (errs.isEmpty()) {
                    true -> ValidationResult(it, interval, true, listOf(msg))
                    else -> ValidationResult(it, interval, false, errs)
                }

            } catch (e: Throwable) {
                e.printStackTrace()
                return@submit ValidationResult(it, System.currentTimeMillis() - start, false, listOf("${e.javaClass}(${e.message})"))
            }
        }
        workerPool.submit(callable) //타입을 명시해준다.
    }
    workerPool.shutdown() //인터럽트는 하지 않음
    return futures.map { it.get()!! }.sortedWith(compareBy({ it.config.group }, { it.config.code }))
}

/** 단순 확인용 */
fun List<ValidationResult>.toGrid(): TextGrid = listOf("그룹", "코드", "설명", "구현설명", "담당자", "검사범위", "걸린시간", "결과", "메세지").toTextGrid(this.map { it.toGridArray() })

/** 단순 확인용 */
fun List<ValidationResult>.toDetailGrid(): TextGrid = listOf("그룹", "코드", "메세지").toTextGrid(this.filter { !it.success }.flatMap { r -> r.msgs.map { arrayOf(r.config.group, r.config.code, it) } })

/** 예외를 던져서 알려줌 */
fun List<ValidationResult>.andThrow(lineSeparator: String = "\n") {
    val invalidResults = this.filter { it.success.not() }
    if (invalidResults.isEmpty()) return
    invalidResults.flatMap { it.msgs }.joinToString(lineSeparator).also { throw ValidationException(it) }
}

fun List<ValidationResult>.printAndThrow() {
    this.toGrid().print()
    if (this.find { !it.success } != null) {
        this.toDetailGrid().print()
    }
    this.andThrow()
}