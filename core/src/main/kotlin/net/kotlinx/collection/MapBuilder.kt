package net.kotlinx.collection


/**
 * 간단 map 작성용 빌더
 * mapOf 사용시 예쁘지 않은점때문에 새로 만듬
 * ex) kotlin CDK에서 JSON 생성 (json 대신 map만 지원함)
 *
 * null 가능한 버전임. 둘다 공존하게는 불가능
 * */
class MapBuilder<T> {

    private val map = mutableMapOf<String, T?>()

    infix fun String.to(value: T?) {
        map[this] = value
    }

    fun build(): Map<String, T?> = map.toMap()
}


inline fun <V> mapOf(block: MapBuilder<V>.() -> Unit): Map<String, V?> = MapBuilder<V>().apply(block).build()

/** 중첩 맵을 위한 함수 */
inline fun <V> MapBuilder<*>.mapOf(block: MapBuilder<V>.() -> Unit): Map<String, V?> = MapBuilder<V>().apply(block).build()