package net.kotlinx.reflect

import aws.smithy.kotlin.runtime.content.BigDecimal
import net.kotlinx.core.string.toLocalDateTime
import net.kotlinx.core.string.toLong2
import java.time.LocalDateTime
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf


/** 타입을 클래스로 강제변환 */
fun KType.toKClass(): KClass<*> = this.classifier as? KClass<*> ?: throw IllegalStateException("변환 불가능 -> $this")

/**
 * 문자열을 타입에 맞는 값으로 바꿔준다
 * 보통 CSV 등에서 입력되는 값을 처리
 *  */
fun KType.from(value: String?): Any? {
    if (value == null && this.isMarkedNullable) return null
    val kClazz: KClass<*> = this.classifier as? KClass<*> ?: throw IllegalStateException("안되는거! $this") //타입을 클래스로 변환 가능 (실패할 수 있음)
    return when {
        kClazz.isSubclassOf(Double::class) -> value!!.toDouble()
        kClazz.isSubclassOf(Int::class) -> value!!.toInt()
        kClazz.isSubclassOf(Long::class) -> value!!.toLong2() // double 형도 반올림해서 강제 변환시킴
        kClazz.isSubclassOf(BigDecimal::class) -> value!!.toBigDecimal()
        kClazz.isSubclassOf(LocalDateTime::class) -> value!!.toLocalDateTime()
        kClazz.java.isEnum -> kClazz.java.enumConstants.filterIsInstance<Enum<*>>().first { it.name == value }
        else -> value  //문자열 그대로 리턴
    }

}
