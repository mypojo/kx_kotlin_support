package net.kotlinx.delegate

import kotlin.reflect.KProperty

/**
 * 메뉴에 속성을 확장할때 사용하는 위임자
 * map 위임의 전형적인 사용 예시이다
 *  */
class MapAttributeDelegate<T> {

    operator fun getValue(thisRef: MapAttribute, property: KProperty<*>): T {
        @Suppress("UNCHECKED_CAST")
        return thisRef.attributes[property.name] as T ?: throw IllegalStateException("menu의 attributes에  ${property.name}가 존재하지 않습니다")
    }

    operator fun setValue(thisRef: MapAttribute, property: KProperty<*>, value: T) {
        thisRef.attributes[property.name] = value as Any
    }
}