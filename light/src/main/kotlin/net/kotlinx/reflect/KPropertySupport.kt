package net.kotlinx.reflect

import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

/**
 * 리플렉션으로 enum값 가져오기
 * 프로퍼티에  ? 줘야지 널 허용하는거도 가능해진다.
 *  */
inline fun <reified T : Enum<T>> KProperty<Enum<T>?>.valueOf(value: String): T? = enumValues<T>().firstOrNull { it.name == value }

/**
 * 프로퍼티 어노테이션과 & 게터 어노테이션  & 자바 어노테이션
 * */
fun KProperty<*>.annotaionAll(): List<Annotation> = this.annotations + this.getter.annotations + (this.javaField?.annotations?.toList() ?: emptyList())