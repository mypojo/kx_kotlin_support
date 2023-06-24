package net.kotlinx.module.reflect

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

//==================================================== 가능하면 리플렉션은 사용하지 말자! ======================================================

/** 새로운 인스턴스를 리턴한다. */
inline fun <T : Any> KClass<T>.newInstance(): T = this.java.getDeclaredConstructor().newInstance() as T

/** 프로퍼티 간단 가져오기 */
inline fun KClass<*>.props(): List<KProperty<*>> = this.members.filterIsInstance<KProperty<*>>()

/**
 * 프로퍼티 간단 가져오기
 * @see net.kotlinx.module.reflect.Bean
 * */
inline fun KClass<*>.prop(name: String): KProperty<*>? = this.members.filterIsInstance<KProperty<*>>().firstOrNull { it.name == name }

/**
 * 특정 어노테이션이 붙어있는지 찾기
 * ex)  p.annotations.findClass(Column::class)
 *  */
inline fun List<Annotation>.findClass(clazz: KClass<out Annotation>): List<Annotation> = this.filter { it.annotationClass == clazz }

/** 가능하면 이거쓸것 */
inline fun <reified T : Annotation> List<Annotation>.findClass(): List<T> = this.filter { it.annotationClass == T::class }.map { it as T }

/**
 * 어노테이션 찾을때 오류나는것들 무시
 * 오류발생시 빈값 리턴 (무시) -> null 대신 사용함
 *  */
inline val KClass<*>.annotationsOrEmpty: List<Annotation>
    get() = try {
        this.annotations
    } catch (e: Throwable) {
        emptyList()
    }