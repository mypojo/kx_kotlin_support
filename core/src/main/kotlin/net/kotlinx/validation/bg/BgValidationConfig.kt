package net.kotlinx.validation.bg

import net.kotlinx.concurrent.parallelExecute
import net.kotlinx.core.Kdsl
import net.kotlinx.domain.developer.DeveloperData
import java.util.concurrent.Callable


interface BgValidation {

    /**
     * 예외발생시 리스트에 추가. (이러면 에러로 간주)
     * 리턴은 성공시의 서머리 메세지
     * */
    fun validate(errs: MutableList<String>): String

    companion object {

        /** 간단 인라인 코드 생성 */
        fun inline(block: (MutableList<String>) -> String): BgValidation {
            return object : BgValidation {
                override fun validate(errs: MutableList<String>): String = block(errs)
            }
        }
    }
}

/**
 * 백그라운드 벨리데이션 설정
 * ex) DB 무결성 체크, 실행환경 검사, 데이터 정합성&오차 검사 등등
 *
 * 요구사항
 * 1. 다수의 위반 발견시, 개별 오류 메시지로 나타나야함
 * 2. 위반 발견이나 예외가 발상하더라도 모든 로직은 작동해야함
 * 3. 설정과 최종 결과를 모아서 확인 가능해야함
 *  */
class BgValidationConfig {

    @Kdsl
    constructor(block: BgValidationConfig.() -> Unit = {}) {
        apply(block)
    }

    /** 그룹 */
    var group: String = "-"

    /** 코드 (영문으로 표기후 시트 정렬에 사용) */
    lateinit var code: String

    /** 설명 (멀티라인보다 구분된 라인이 더 보기 좋아서 선택) */
    var desc: List<String> = emptyList()

    /** 구현 설명 ex)  DB와 Athena를 비교 */
    var codeDesc: String = ""

    /** 벨리데이션 대상 범위. 매번 전체를 하면 너무 느림 */
    var range: BgValidationType = BgValidationType.NOW

    /** 담당자 ID */
    var authors: Collection<DeveloperData> = emptySet()

    /** kotin 의존성만 주입 가능하다. spring 등은 일단 고려하지 않음 */
    lateinit var validationCode: BgValidation
}

/**
 * 내부 로직에 선언적 트랜잭션이 자주 사용되기 때문에 스래드풀을 사용해서 실행함
 *  */
fun List<BgValidationConfig>.validateAll(threadCnt: Int = Runtime.getRuntime().availableProcessors()): List<BgValidationResult> {

    val results = this.map {
        Callable {
            val start = System.currentTimeMillis()
            try {
                val errs = mutableListOf<String>()
                val msg = it.validationCode.validate(errs)
                val interval = System.currentTimeMillis() - start
                when (errs.isEmpty()) {
                    true -> BgValidationResult(it, interval, true, listOf(msg))
                    else -> BgValidationResult(it, interval, false, errs)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                BgValidationResult(it, System.currentTimeMillis() - start, false, listOf("${e.javaClass}(${e.message})"))
            }
        }
    }.parallelExecute(threadCnt)

    return results.sortedWith(compareBy({ it.config.group }, { it.config.code }))
}
