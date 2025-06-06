package net.kotlinx.validation.repeated

import kotlinx.coroutines.runBlocking
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.concurrent.parallelExecute
import net.kotlinx.string.toTextGridPrint
import java.util.concurrent.Callable


/** 설정 확인용 */
fun List<RepeatedValidation>.print() {
    listOf("그룹", "코드", "설명", "담당자", "검사범위").toTextGridPrint { this.map { it.toGridArray() } }
}

//==================================================== 실행 2가지 ======================================================

/**
 * 내부 로직에 선언적 트랜잭션이 자주 사용되기 때문에 스래드풀을 사용해서 실행함
 *  */
fun List<RepeatedValidation>.validateAllByThread(threadCnt: Int = Runtime.getRuntime().availableProcessors()): List<RepeatedValidationResult> =
    this.map { Callable { runBlocking { it.validate() } } }.parallelExecute(threadCnt).sort()

/**
 * DB가 없는 로직에서 사용
 *  */
fun List<RepeatedValidation>.validateAllByCoroutine(threadCnt: Int = Runtime.getRuntime().availableProcessors()): List<RepeatedValidationResult> =
    this.map { suspend { it.validate() } }.coroutineExecute(threadCnt).sort()