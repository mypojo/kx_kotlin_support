package net.kotlinx.flow

import kotlinx.coroutines.flow.*

/**
 * 병렬 처리를 위한 단축 메소드
 * 자주 까먹어서 마킹용으로 기록함
 *
 * flow 파이프라인은 SFN 처럼 병렬 / 순차 처리를 유연하게 조합 가능함으로 코루틴 직접사용보다 권장되나, 약간의 오버헤드가 있음
 *
 * 에러가 나면 즉시 전체가 중단됨.
 *  #1 에러가 안나게 catch로 무조건 감싸던가 -> 에러나면 실패 emit()
 *  #2 .catch {} 를 달아서 emit() 후  collect 에서 마지막 메세지를 처리하던가 (이건 쓸일 없을듯..)
 * */
suspend fun <T, R> Flow<T>.execute(concurrency: Int = DEFAULT_CONCURRENCY, transform: suspend (T) -> Flow<R>): List<R> = this.flatMapMerge(concurrency, transform).toList()

/** collect 의 편의버전 */
suspend fun <T> Flow<T>.collectIt(block: () -> FlowCollector<T>) = this.collect(block())

/**
 * Flow에서 랜덤하게 n개 항목을 추출
 * 대용량 데이터에서, 인메모리에 들어갈만큼 표본수를 줄이기위해 사용된다.
 * @param n  메모리에 들어갈만큼 충분히 작아야함
 */
fun <T> Flow<T>.randomSample(n: Int, headerCnt: Int = 0): Flow<T> = flow {
    // 레저보어 샘플링(Reservoir Sampling) 알고리즘 사용
    val reservoir = mutableListOf<T>()
    var count = 0

    val headers = mutableListOf<T>()

    collect { item ->

        if (headers.size < headerCnt) {
            headers.add(item)
            return@collect
        }

        count++
        if (reservoir.size < n) {
            // 처음 n개 항목은 바로 저장
            reservoir.add(item)
        } else {
            // n개 이후의 항목은 확률적으로 교체
            val random = kotlin.random.Random.nextInt(count)
            if (random < n) {
                reservoir[random] = item
            }
        }
    }

    // 랜덤하게 섞기
    reservoir.shuffle()

    // 결과 방출
    (headers + reservoir).forEach { emit(it) }
}
