package net.kotlinx.module1.reflect

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.core.gson.GsonSet
import net.kotlinx.core.string.toLocalDateTime
import java.time.LocalDateTime
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

/** 단일값 문자열로 치환해서  가져오기 */
inline fun <T> Map<String, AttributeValue>.find(key: String, conv: (data: String) -> T?): T? = this[key]?.asSOrNull()?.let { conv.invoke(it) }

//==================================================== Long ======================================================
inline fun Map<String, AttributeValue>.find(key: KMutableProperty1<*, Long?>): Long? = this[key.name]?.asNOrNull()?.toLong()
inline fun Map<String, AttributeValue>.findOrThrow(key: KMutableProperty1<*, Long>): Long = this[key.name]?.asNOrNull()?.toLong()
    ?: throw IllegalArgumentException("[${key.name}] not found")

//==================================================== Int ======================================================
inline fun Map<String, AttributeValue>.find(key: KMutableProperty1<*, Int?>): Int? = this[key.name]?.asNOrNull()?.toInt()
inline fun Map<String, AttributeValue>.findOrThrow(key: KMutableProperty1<*, Int>): Int = this[key.name]?.asNOrNull()?.toInt()
    ?: throw IllegalArgumentException("[${key.name}] not found")

//==================================================== String ======================================================
inline fun Map<String, AttributeValue>.find(key: KMutableProperty1<*, String?>): String? = this[key.name]?.asSOrNull()
inline fun Map<String, AttributeValue>.findOrThrow(key: KMutableProperty1<*, String>): String = this[key.name]?.asSOrNull()
    ?: throw IllegalArgumentException("[${key.name}] not found")

//==================================================== enum ======================================================
inline fun <reified T : Enum<T>> Map<String, AttributeValue>.find(key: KProperty<Enum<T>?>): T? = this.find(key.name) { key.valueOf(it) }
inline fun <reified T : Enum<T>> Map<String, AttributeValue>.findOrThrow(key: KProperty<Enum<T>?>): T = this.find(key.name) { key.valueOf(it) }
    ?: throw IllegalArgumentException("[${key.name}] not found")

//==================================================== LocalDateTime ======================================================
inline fun Map<String, AttributeValue>.find(key: KMutableProperty1<*, LocalDateTime?>): LocalDateTime? = this.find(key.name) { it.toLocalDateTime() }
inline fun Map<String, AttributeValue>.findOrThrow(key: KMutableProperty1<*, LocalDateTime>): LocalDateTime = this.find(key.name) { it.toLocalDateTime() }
    ?: throw IllegalArgumentException("[${key.name}] not found")

//==================================================== json ======================================================
inline fun <reified T> Map<String, AttributeValue>.findJson(key: KMutableProperty1<*, T?>): T? = this.find(key.name) { GsonSet.GSON.fromJson(it, T::class.java) }
inline fun <reified T> Map<String, AttributeValue>.findJsonOrThrow(key: KMutableProperty1<*, T>): T = this.find(key.name) { GsonSet.GSON.fromJson(it, T::class.java) }
    ?: throw IllegalArgumentException("[${key.name}] not found")


//==================================================== map ======================================================

inline fun Map<String, AttributeValue>.find(key: KMutableProperty1<*, Map<String, String>?>): Map<String, String> =
    this[key.name]?.asM()?.entries?.associate { it.key to it.value.asS() } ?: emptyMap()

inline fun Map<String, AttributeValue>.findOrThrow(key: KMutableProperty1<*, Map<String, String>>): Map<String, String> =
    this[key.name]?.asM()?.entries?.associate { it.key to it.value.asS() } ?: throw IllegalArgumentException("[${key.name}] not found")