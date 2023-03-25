package net.kotlinx.module1.reflect

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.core1.string.toLocalDateTime
import java.time.LocalDateTime
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

/** 단일값 문자열로 치환해서  가져오기 */
inline fun <T> Map<String, AttributeValue>.find(key: String, conv: (data: String) -> T?): T? = this[key]?.asS()?.let { conv.invoke(it) }

/** Long */
inline fun Map<String, AttributeValue>.find(key: KMutableProperty1<*, Long?>): Long? = this[key.name]?.asNOrNull()?.let { it.toLong() }

/** Int */
inline fun Map<String, AttributeValue>.find(key: KMutableProperty1<*, Int?>): Int? = this[key.name]?.asNOrNull()?.let { it.toInt() }

/** String */
inline fun Map<String, AttributeValue>.find(key: KMutableProperty1<*, String?>): String? = this[key.name]?.asSOrNull()

/** enum */
inline fun <reified T : Enum<T>> Map<String, AttributeValue>.find(key: KProperty<Enum<T>>): T? = this[key.name]?.asS()?.let { key.valueOf(it) }

/** LocalDateTime */
inline fun Map<String, AttributeValue>.find(key: KMutableProperty1<*, LocalDateTime?>): LocalDateTime? = this[key.name]?.asS()?.let { it.toLocalDateTime() }
