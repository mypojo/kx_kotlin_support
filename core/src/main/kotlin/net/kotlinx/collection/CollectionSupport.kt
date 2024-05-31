package net.kotlinx.collection

/**
 * 내부에 Iterable 이 존재한다면 펼쳐준다.
 * ex) listOf 사용시 내무에 addAll이 필요한 경우 일단 넣고 펼쳐줌
 *  */
fun Collection<Any>.flattenAny(): List<Any> = this.flatMap {
    if (it is Iterable<*>) it
    else listOf(it)
}.filterNotNull()


/** 둘 사이에 다른점을 리턴 */
fun <T> Collection<T>.diff(other: Collection<T>): Set<T> {
    val a = this.toSet()
    val b = other.toSet()
    val sum = (a + b)
    return (sum - a) + (sum - b)
}

/**
 * 없길래 추가함.
 * 문자열이 비어있지 않은것만 필터링
 *  */
fun <T> Collection<T>.mapNotEmpty(transform: (T) -> String?): Collection<String> = this.mapNotNull(transform).filter { it.isNotEmpty() }


/**
 * 첫 조건에따라 그룹바이한다.
 * ex) 라인 파싱
 *  */
fun <T> List<T>.groupByFirstCondition(transform: (T) -> Boolean): List<List<T>> {
    val results: MutableList<List<T>> = mutableListOf()
    var existList = mutableListOf<T>()
    for (current in this) {
        val start = transform(current)
        if (start) {
            results += existList
            existList = mutableListOf()
        }
        existList += current
    }
    results += existList //마지막 남은거 넣어줌
    return results.filter { it.isNotEmpty() }
}