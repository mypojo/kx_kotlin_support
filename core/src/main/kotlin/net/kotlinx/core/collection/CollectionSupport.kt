package net.kotlinx.core.collection

/**
 * 내부에 Iterable 이 존재한다면 펼쳐준다.
 * ex) listOf 사용시 내무에 addAll이 필요한 경우 일단 넣고 펼쳐줌
 *  */
fun Collection<Any>.flattenAny(): List<Any> = this.flatMap {
    if (it is Iterable<*>) it
    else listOf(it)
}.filterNotNull()