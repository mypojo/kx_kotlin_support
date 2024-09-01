package net.kotlinx.aws.dynamo.query

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.core.Kdsl


/**
 * 쿼리 표현식 샘플
 * */
object DynamoExpressionSet {

    /** PK 접두사로 스캐닝 (쿼리는 이거 불가능) */
    class PkPrefix : DynamoExpression {

        @Kdsl
        constructor(block: PkPrefix.() -> Unit = {}) {
            apply(block)
        }

        override fun filterExpression(): String? = "begins_with ($pkName, :$pkName)"

        override fun expressionAttributeValues(): Map<String, AttributeValue> = mapOf(
            ":$pkName" to AttributeValue.S(pk)
        )

    }

    /**
     * PK & SK 동일한거
     * SK 가 있을경우 SK도 비교해줌 ( 인덱스는 SK 동일하게 여러개 넣을 수 있음)
     *  */
    class PkSkEq : DynamoExpression {

        @Kdsl
        constructor(block: PkSkEq.() -> Unit = {}) {
            apply(block)
        }

        override fun keyConditionExpression(): String = if (sk == null) "$pkName = :${pkName}" else "$pkName = :${pkName} AND $skName = :${skName}"

        override fun expressionAttributeValues(): Map<String, AttributeValue> = buildMap {
            put(":${pkName}", AttributeValue.S(pk))
            if (sk != null) put(":${skName}", AttributeValue.S(sk!!))
        }

    }

    /** SK 접두사 */
    class SkPrefix : DynamoExpression {

        @Kdsl
        constructor(block: SkPrefix.() -> Unit = {}) {
            apply(block)
        }

        override fun keyConditionExpression(): String = "$pkName = :${pkName} AND begins_with (${skName}, :${skName})"

        override fun expressionAttributeValues(): Map<String, AttributeValue> = mapOf(
            ":${pkName}" to AttributeValue.S(pk),
            ":${skName}" to AttributeValue.S(sk!!),
        )

    }

    /** SK 접두사 여러개 */
    class SkPrefixIn : DynamoExpression {

        lateinit var dd: String

        @Kdsl
        constructor(block: SkPrefixIn.() -> Unit = {}) {
            apply(block)
        }

        override fun keyConditionExpression(): String = "$pkName = :${pkName} AND (begins_with (${skName}, :${skName}) or begins_with (${skName}, :dd ) )   "

        override fun expressionAttributeValues(): Map<String, AttributeValue> = mapOf(
            ":${pkName}" to AttributeValue.S(pk),
            ":${skName}" to AttributeValue.S(sk!!),
            ":dd" to AttributeValue.S(dd!!),
        )

    }


}