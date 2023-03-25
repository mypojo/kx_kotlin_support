package net.kotlinx.module1.reflect

import kotlin.reflect.KClass

inline fun <T : Any> KClass<T>.newInstance(): T = this.java.getDeclaredConstructor().newInstance() as T