package net.kotlinx.module.reflect

import kotlin.reflect.KType

/**
 * 상위 타입을 내부 저장하는 객체 샘플
 * ex) Hibernate Repository
 * */
abstract class SuperTypeToken<T> {

    val type: KType = this::class.supertypes[0].arguments[0].type!!

}