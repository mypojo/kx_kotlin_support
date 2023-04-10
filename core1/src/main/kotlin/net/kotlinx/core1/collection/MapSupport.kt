package net.kotlinx.core1.collection


/**
 * map을 펼쳐서 생성자에 넣을 수 있게 해준다. map에 데이터를 추가해서 새로운 map을 만들때 사용
 * ex) mapOf( *map1.pairs,..)
 * 참고로 + 로 더해도 됨.
 *  */
inline fun <K, V> Map<K, V>.pairs(): Array<Pair<K, V>> = entries.map { it.toPair() }.toTypedArray()