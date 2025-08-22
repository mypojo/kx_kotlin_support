package net.kotlinx.delegate

import kotlin.reflect.KProperty

/**
 * 메뉴에 속성을 확장할때 사용하는 위임자
 * map 위임의 전형적인 사용 예시이다
 *  */
class MapAttributeDelegateNull<T> {

    operator fun getValue(thisRef: MapAttribute, property: KProperty<*>): T? {
        return thisRef.attributes[property.name] as T
    }

    operator fun setValue(thisRef: MapAttribute, property: KProperty<*>, value: T?) {
        thisRef.attributes[property.name] = value as Any
    }
}