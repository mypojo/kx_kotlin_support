package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.json.gson.GsonData
import net.kotlinx.json.gson.GsonSet
import net.kotlinx.json.gson.toGsonData
import net.kotlinx.reflect.valueOf
import net.kotlinx.string.toLocalDateTime
import java.time.LocalDateTime
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/** 단일값 문자열로 치환해서  가져오기 */
fun <T> Map<String, AttributeValue>.find(key: String, conv: (data: String) -> T?): T? = this[key]?.asSOrNull()?.let { conv.invoke(it) }

//==================================================== GsonData ======================================================
fun Map<String, AttributeValue>.find(key: KProperty1<*, GsonData?>): GsonData? = this[key.name]?.asSOrNull()?.toGsonData()
fun Map<String, AttributeValue>.findOrThrow(key: KProperty1<*, GsonData>): GsonData = this[key.name]?.asSOrNull()?.toGsonData()
    ?: throw IllegalArgumentException("[${key.name}] not found")

fun MutableMap<String, AttributeValue>.add(key: KProperty1<*, GsonData>, value: GsonData) {
    this += key.name to AttributeValue.S(value.toString())
}

//==================================================== Bool ======================================================
fun Map<String, AttributeValue>.find(key: KProperty1<*, Boolean?>): Boolean? = this[key.name]?.asBoolOrNull()
fun Map<String, AttributeValue>.findOrThrow(key: KProperty1<*, Boolean>): Boolean = this[key.name]?.asBoolOrNull()
    ?: throw IllegalArgumentException("[${key.name}] not found")

//==================================================== Long ======================================================
fun Map<String, AttributeValue>.find(key: KProperty1<*, Long?>): Long? = this[key.name]?.asNOrNull()?.toLong()
fun Map<String, AttributeValue>.findOrThrow(key: KProperty1<*, Long>): Long = this[key.name]?.asNOrNull()?.toLong()
    ?: throw IllegalArgumentException("[${key.name}] not found")

fun MutableMap<String, AttributeValue>.add(key: KProperty1<*, Long?>, value: Long?) {
    value?.let { this += key.name to AttributeValue.N(value.toString()) }
}

//==================================================== Int ======================================================
fun Map<String, AttributeValue>.find(key: KProperty1<*, Int?>): Int? = this[key.name]?.asNOrNull()?.toInt()
fun Map<String, AttributeValue>.findOrThrow(key: KProperty1<*, Int>): Int = this[key.name]?.asNOrNull()?.toInt()
    ?: throw IllegalArgumentException("[${key.name}] not found")

//==================================================== String ======================================================
fun Map<String, AttributeValue>.find(key: KProperty1<*, String?>): String? = this[key.name]?.asSOrNull()
fun Map<String, AttributeValue>.findOrThrow(key: KProperty1<*, String>): String = this[key.name]?.asSOrNull()
    ?: throw IllegalArgumentException("[${key.name}] not found")

fun MutableMap<String, AttributeValue>.add(key: KProperty1<*, String?>, value: String?) {
    value?.let { this += key.name to AttributeValue.S(value) }
}

//==================================================== enum ======================================================
inline fun <reified T : Enum<T>> Map<String, AttributeValue>.find(key: KProperty<Enum<T>?>): T? = this.find(key.name) { key.valueOf(it) }
inline fun <reified T : Enum<T>> Map<String, AttributeValue>.findOrThrow(key: KProperty<Enum<T>?>): T = this.find(key.name) { key.valueOf(it) }
    ?: throw IllegalArgumentException("[${key.name}] not found")

//==================================================== LocalDateTime ======================================================
fun Map<String, AttributeValue>.find(key: KProperty1<*, LocalDateTime?>): LocalDateTime? = this.find(key.name) { it.toLocalDateTime() }
fun Map<String, AttributeValue>.findOrThrow(key: KProperty1<*, LocalDateTime>): LocalDateTime = this.find(key.name) { it.toLocalDateTime() }
    ?: throw IllegalArgumentException("[${key.name}] not found")

//==================================================== json ======================================================
inline fun <reified T> Map<String, AttributeValue>.findJson(key: KProperty1<*, T?>): T? = this.find(key.name) { GsonSet.GSON.fromJson(it, T::class.java) }
inline fun <reified T> Map<String, AttributeValue>.findJsonOrThrow(key: KProperty1<*, T>): T = this.find(key.name) { GsonSet.GSON.fromJson(it, T::class.java) }
    ?: throw IllegalArgumentException("[${key.name}] not found")

//==================================================== map ======================================================

fun Map<String, AttributeValue>.find(key: KProperty1<*, Map<String, String>?>): Map<String, String> =
    this[key.name]?.asM()?.entries?.associate { it.key to it.value.asS() } ?: emptyMap()

fun Map<String, AttributeValue>.findOrThrow(key: KProperty1<*, Map<String, String>>): Map<String, String> =
    this[key.name]?.asM()?.entries?.associate { it.key to it.value.asS() } ?: throw IllegalArgumentException("[${key.name}] not found")

//==================================================== pair  ======================================================

/** PK / SK 입력 */
fun MutableMap<String, AttributeValue>.add(key: KProperty1<*, Pair<String, String>?>, pair: Pair<String, String>?) {
    pair?.let {
        this += "${key.name}Pk" to AttributeValue.S(it.first)
        this += "${key.name}Sk" to AttributeValue.S(it.second)
    }
}

/** PK / SK 로드 */
fun Map<String, AttributeValue>.findPair(key: KProperty1<*, Pair<String, String>?>): Pair<String, String>? {
    val pk = this["${key.name}Pk"]?.asSOrNull()
    val sk = this["${key.name}Sk"]?.asSOrNull()
    return if (pk == null || sk == null) return null else pk to sk
}

