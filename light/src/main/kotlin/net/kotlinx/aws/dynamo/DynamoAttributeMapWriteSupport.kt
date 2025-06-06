package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.json.gson.GsonData
import net.kotlinx.time.toIso
import java.time.LocalDateTime


//==================================================== 간단 입력 ======================================================

fun MutableMap<String, AttributeValue>.put(key: String, value: String?) {
    if (value == null) return
    this += key to AttributeValue.S(value)
}

fun MutableMap<String, AttributeValue>.put(key: String, value: GsonData?) {
    if (value == null) return
    this += key to AttributeValue.S(value.toString())
}

fun MutableMap<String, AttributeValue>.put(key: String, value: Number?) {
    if (value == null) return
    this += key to AttributeValue.N(value.toString())
}

fun MutableMap<String, AttributeValue>.put(key: String, value: Boolean?) {
    if (value == null) return
    this += key to AttributeValue.Bool(value)
}

/** 날짜는 문자열로 저장 */
fun MutableMap<String, AttributeValue>.put(key: String, value: LocalDateTime?) {
    if (value == null) return
    this += key to AttributeValue.S(value.toIso())
}

/** enum은 문자열로 저장 */
fun MutableMap<String, AttributeValue>.put(key: String, value: Enum<*>?) {
    if (value == null) return
    this += key to AttributeValue.S(value.name)
}
