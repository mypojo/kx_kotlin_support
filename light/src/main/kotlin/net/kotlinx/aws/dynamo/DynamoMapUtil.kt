package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue

/**
 * DynamoDB AttributeValue <-> Kotlin Map 변환 유틸리티
 * - Map<String, Any> 를 AttributeValue.M 으로 변환
 * - AttributeValue.M 을 Map<String, Any> 로 복원
 * - 숫자 타입은 DDB의 특성에 맞춰 우선 Long, 실패 시 Double 로 파싱
 */
object DynamoMapUtil {

    /** 맵을 AttributeValue 로 변환해  */
    fun toAttribute(src: Map<String, Any>): AttributeValue = toAttributeValue(src)

    /** 범용 변환기. 보통 map에 사용 */
    fun toAttributeValue(v: Any): AttributeValue = when (v) {
        is AttributeValue -> v
        is String -> AttributeValue.S(v)
        is Boolean -> AttributeValue.Bool(v)
        is Int, is Long -> AttributeValue.N((v as Number).toLong().toString())
        is Float, is Double -> AttributeValue.N((v as Number).toDouble().toString())
        is Number -> AttributeValue.N(v.toString())
        is Map<*, *> -> {
            @Suppress("UNCHECKED_CAST")
            val m = (v as Map<String, Any?>)
                .mapValues { (_, vv) -> vv?.let { toAttributeValue(it) } }
                .filterValues { it != null } as Map<String, AttributeValue>
            AttributeValue.M(m)
        }

        is Iterable<*> -> AttributeValue.L(v.mapNotNull { it?.let { toAttributeValue(it) } })
        else -> AttributeValue.S(v.toString())
    }

    /** MapAttribute(M) -> Map<String, Any> (재귀) */
    fun fromAttributeMap(att: AttributeValue?): Map<String, Any> {
        if (att == null) return emptyMap()
        val m = att.asMOrNull() ?: throw IllegalArgumentException("Dynamo의 M이 아닙니다. $att")
        return buildMap {
            m.forEach { (k, av) ->
                put(k, fromAttributeMapValue(av))
            }
        }
    }

    fun fromAttributeMapValue(av: AttributeValue): Any = when {
        av.asSOrNull() != null -> av.asS()
        av.asBoolOrNull() != null -> av.asBool()
        av.asNOrNull() != null -> parseNumber(av.asN()) //숫자는 다 Long 으로 간주
        av.asMOrNull() != null -> fromAttributeMap(av)
        av.asLOrNull() != null -> av.asL().map { fromAttributeMapValue(it) }
        else -> av.toString()
    }

    private fun parseNumber(num: String): Long = num.toLongOrNull() ?: num.toLong()
}
