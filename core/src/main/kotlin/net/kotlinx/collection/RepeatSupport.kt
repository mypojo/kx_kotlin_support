package net.kotlinx.collection

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


/**
 * 리턴값(보통 next_token)이 없을때까지 반복한다.
 * keep : 결과 누적객체
 * nextToken : 입력할 다음토큰. 최초에는 null
 * return : 다음 토큰.  null이면 중지
 *  */
suspend fun <T> repeatCollectUntil(
    maxTimes: Int = 100, block: suspend (keep: MutableList<List<T>>, nextToken: String?) -> String?
): List<List<T>> {
    val results = mutableListOf<List<T>>()
    var nextToken: String? = null
    repeat(maxTimes) {
        nextToken = block(results, nextToken)
        if (nextToken == null) {
            return results
        }
    }
    return results
}


/**
 * 최대 X회 까지 작업을 반복한다.
 * ex) job이 종료되었는지 1분 주기로 체크
 * @param maxTimes 무한루프 방지용
 * @param block true 성공으로 간주하고 이면 중단한다.
 * */
suspend fun repeatUntil(maxTimes: Int, block: suspend (Int) -> Boolean): Int {
    for (i in 0..maxTimes) {
        val result = block(i)
        if (result) return i
    }
    throw IllegalStateException("repeat maxTimes $maxTimes 를 초과했습니다.")
}

