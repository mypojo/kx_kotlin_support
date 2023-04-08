package net.kotlinx.core1.lang

import kotlin.reflect.KClass
import kotlin.reflect.KType


/** 타입을 클래스로 강제변환 */
fun KType.toKClass(): KClass<*> = this.classifier as? KClass<*> ?: throw IllegalStateException("변환 불가능 -> $this")