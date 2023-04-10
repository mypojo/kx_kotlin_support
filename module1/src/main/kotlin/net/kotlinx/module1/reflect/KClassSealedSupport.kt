package net.kotlinx.module1.reflect

import kotlin.reflect.KClass

/**
 * 해당 클래스의 sealed instance 를 반환한다
 * enum을 대신할때 사용
 * class<T> 와 reified T 가 다른경우가 많으니 주의!
 *  */
inline fun <reified T> KClass<*>.sealedinstances(): List<T> = this.sealedSubclasses.mapNotNull { it.objectInstance as? T }
