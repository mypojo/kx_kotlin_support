package net.kotlinx.module1.dto

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

/**
 * 코틀린 리플렉션은 가능하면 사용하지 말것
 * */
object ReflectionUtil {

    /**
     * @param to 기본 생성자가 있어야함
     * */
    fun <T : Any> convert(from: Any, to: KClass<T>): T {
        val fromMap = from::class.members.filterIsInstance<KProperty<*>>().associateBy { it.name }
        val newInstance = to.constructors.firstOrNull { it.parameters.isEmpty() }?.let { it.call() } ?: throw IllegalArgumentException("기본 생성자가 있어야 합니다 : $to")
        to.members.filterIsInstance<KMutableProperty<*>>().forEach { toField ->
            val value = fromMap[toField.name]?.getter?.call(from)
            value?.let { toField.setter.call(newInstance, it) }
        }
        return newInstance
    }

}
