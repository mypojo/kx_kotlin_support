package net.kotlinx.module1.reflect

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

/**
 * * 간단한 리플렉션 도구
 * 패키지 크기가 커서 의존관계 하위로 이동시킴
 * */
object ReflectionUtil {

    /**
     * 객체간 변환을 도와준다.
     * @param to 기본 생성자가 있어야함
     * */
    fun <T : Any> convert(from: Any, to: KClass<T>): T {
        val fromMap = from::class.members.filterIsInstance<KProperty<*>>().associateBy { it.name }
        val newInstance = to.constructors.firstOrNull { it.parameters.isEmpty() }?.call() ?: throw IllegalArgumentException("기본 생성자가 있어야 합니다 : $to")
        to.members.filterIsInstance<KMutableProperty<*>>().forEach { toField ->
            val value = fromMap[toField.name]?.getter?.call(from)
            value?.let { toField.setter.call(newInstance, it) }
        }
        return newInstance
    }

}
