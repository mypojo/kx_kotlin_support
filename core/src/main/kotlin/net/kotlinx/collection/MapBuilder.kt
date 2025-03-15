package net.kotlinx.collection


/**
 * 간단 map 작성용 빌더
 * mapOf 사용시 예쁘지 않은점때문에 새로 만듬
 * ex) kotlin CDK에서 JSON 생성 (json 대신 map만 지원함)
 * */
class MapBuilder {

    private val map = mutableMapOf<String, Any?>()

    infix fun String.to(value: Any?) {
        map[this] = value
    }

    fun build(): Map<String, Any?> = map.toMap()
}


inline fun mapOf(block: MapBuilder.() -> Unit): Map<String, Any?> {
    return MapBuilder().apply(block).build()
}

// 중첩 맵을 위한 함수
inline fun MapBuilder.mapOf(block: MapBuilder.() -> Unit): Map<String, Any?> {
    return MapBuilder().apply(block).build()
}