package net.kotlinx.validation.repeated

import kotlinx.coroutines.runBlocking
import net.kotlinx.concurrent.coroutineExecute
import net.kotlinx.concurrent.parallelExecute
import net.kotlinx.string.toTextGridPrint
import java.util.concurrent.Callable


/**
 * 하나의 성공메세지 or 다수의 실패 메세지가 존재할 수 있는 벨리데이터 정의 (공용)
 * 폼이나 로직 벨리데이션은 이미 잘 만들어진 모듈이 있음
 * 이건 백그라운드 로직 등에서 사용할 용도임
 *
 * 예외발생시 리스트에 추가. -> 리스트에 하나라도 추가되면 에러로 간주. 반대로 비어있으면 성공으로 간주
 * 리턴은 성공 메세지
 * */
typealias RepeatedValidator = suspend (MutableList<String>) -> String

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