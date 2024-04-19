package net.kotlinx.core.collection

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