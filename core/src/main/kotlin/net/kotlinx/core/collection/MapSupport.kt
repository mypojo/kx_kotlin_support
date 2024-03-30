package net.kotlinx.core.collection

import net.kotlinx.core.string.encodeUrl


/**
 * map을 펼쳐서 생성자에 넣을 수 있게 해준다. map에 데이터를 추가해서 새로운 map을 만들때 사용
 * ex) mapOf( *map1.pairs,..)
 * 참고로 + 로 더해도 됨.
 *  */
fun <K, V> Map<K, V>.pairs(): Array<Pair<K, V>> = entries.map { it.toPair() }.toTypedArray()

/**
 * 간이 쿼리 스트링 제작기
 * 딱 쿼리스트링만 필요할때 사용
 * 다른 이유라면 okHttp 를 사용하자.
 * */
fun Map<String, String?>.toQueryString(sep: String = "&"): String = this.map { (k, v) -> "$k=${v?.encodeUrl() ?: ""}" }.joinToString(sep)


/**
 * 비어있지 않은경우 ? 를 포함해서 전달해준다.
 *  */
fun Map<String, String?>.toFullQueryString(): String = when (this.isEmpty()) {
    true -> ""
    false -> "?${this.toQueryString("&")}" //http의 경우 & 고정
}