package net.kotlinx.module.reflect

import kotlin.reflect.KProperty

/**
 * 리플렉션으로 enum값 가져오기
 * 프로퍼티에  ? 줘야지 널 허용하는거도 가능해진다.
 *  */
inline fun <reified T : Enum<T>> KProperty<Enum<T>?>.valueOf(value: String): T? = enumValues<T>().firstOrNull { it.name == value }

/** 프로퍼티 어노테이션과, 게터 어노테이션 둘다 가져오기 */
inline fun KProperty<*>.annotaionAll(): List<Annotation> = this.annotations + this.getter.annotations