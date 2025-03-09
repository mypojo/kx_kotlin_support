package net.kotlinx.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow


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
