package net.kotlinx.core1.lang

import kotlin.reflect.KClass

/** 새로운 인스턴스를 리턴한다. */
fun <T : Any> KClass<T>.newInstance(): T = this.java.getDeclaredConstructor().newInstance() as T