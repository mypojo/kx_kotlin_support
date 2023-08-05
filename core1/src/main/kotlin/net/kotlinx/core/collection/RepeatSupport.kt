package net.kotlinx.core.collection

/**
 * maxTimes 만큼 최대로 반복해서 결과를 리턴한다. (무한반복 금지)
 * ex) 조회 데이터가 더 없을때까지 데이터 조회 ex) 페이징 API
 * @param block 리소스가 들어가는 작업
 * @return flatten() 해서 쓰세요~
 * */
fun <T> repeatCollectUntilEmpty(maxTimes: Int = 100, block: (Int) -> List<T>): List<List<T>> {
    val results = mutableListOf<List<T>>()
    repeat(maxTimes) {
        val result = block(it)
        if (result.isEmpty()) {
            return results
        }
        results += result
    }
    return results
}