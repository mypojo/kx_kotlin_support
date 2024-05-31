package net.kotlinx.validation.bg

import kotlinx.coroutines.runBlocking
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.concurrent.parallelExecute
import net.kotlinx.core.Kdsl
import net.kotlinx.domain.developer.DeveloperData
import net.kotlinx.string.abbr
import net.kotlinx.string.toTextGridPrint
import net.kotlinx.validation.Validator
import java.util.concurrent.Callable


/**
 * 백그라운드 벨리데이션 설정
 * ex) DB 무결성 체크, 실행환경 검사, 데이터 정합성&오차 검사 등등
 * ex) 대량 크롤링으로 상품 재고 판단 확인
 *
 * 요구사항
 * 1. 다수의 위반 발견시, 개별 오류 메시지로 나타나야함
 * 2. 위반 발견이나 예외가 발상하더라도 모든 로직은 작동해야함
 * 3. 설정과 최종 결과를 모아서 확인 가능해야함
 *  */
class BgValidation {

    @Kdsl
    constructor(block: BgValidation.() -> Unit = {}) {
        apply(block)
    }

    /** 그룹 */
    var group: String = "-"

    /** 코드 (영문으로 표기후 시트 정렬에 사용) */
    lateinit var code: String

    /** 설명 (멀티라인보다 구분된 라인이 더 보기 좋아서 선택) */
    var desc: List<String> = emptyList()

    /** 벨리데이션 대상 범위. 매번 전체를 하면 너무 느림 */
    var range: BgValidationType = BgValidationType.RUNTIME

    /** 담당자 ID */
    var authors: Collection<DeveloperData> = emptySet()

    /** kotin 의존성만 주입 가능하다. spring 등은 일단 고려하지 않음 */
    lateinit var validator: Validator

    /** 벨리데이션 실행 */
    suspend fun validate(): BgValidationResult {
        val start = System.currentTimeMillis()
        return try {
            val errs = mutableListOf<String>()
            val msg = validator(errs)
            val interval = System.currentTimeMillis() - start
            when (errs.isEmpty()) {
                true -> BgValidationResult(this, interval, true, listOf(msg))
                else -> BgValidationResult(this, interval, false, errs)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            BgValidationResult(this, System.currentTimeMillis() - start, false, listOf("${e.javaClass}(${e.message})"))
        }
    }

    fun toGridArray(): Array<*> {
        return arrayOf(
            group,
            code,
            desc.joinToString(",").abbr(60),
            authors,
            range,
        )
    }
}

/** 설정 확인용 */
fun List<BgValidation>.print() {
    listOf("그룹", "코드", "설명", "담당자", "검사범위").toTextGridPrint { this.map { it.toGridArray() } }
}

//==================================================== 실행 2가지 ======================================================

/**
 * 내부 로직에 선언적 트랜잭션이 자주 사용되기 때문에 스래드풀을 사용해서 실행함
 *  */
fun List<BgValidation>.validateAllByThread(threadCnt: Int = Runtime.getRuntime().availableProcessors()): List<BgValidationResult> =
    this.map { Callable { runBlocking { it.validate() } } }.parallelExecute(threadCnt).sort()

/**
 * DB가 없는 로직에서 사용
 *  */
fun List<BgValidation>.validateAllByCoroutine(threadCnt: Int = Runtime.getRuntime().availableProcessors()): List<BgValidationResult> =
    this.map { suspend { it.validate() } }.coroutineExecute(threadCnt).sort()