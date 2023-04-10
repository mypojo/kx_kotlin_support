package net.kotlinx.module1.reflect

import kotlin.reflect.KProperty

/**
 * 리플렉션으로 enum값 가져오기
 * 프로퍼티에  ? 줘야지 널 허용하는거도 가능해진다.
 *  */
inline fun <reified T : Enum<T>> KProperty<Enum<T>?>.valueOf(value: String): T? = enumValues<T>().firstOrNull { it.name == value }



