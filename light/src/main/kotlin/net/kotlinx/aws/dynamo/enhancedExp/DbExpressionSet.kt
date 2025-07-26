package net.kotlinx.aws.dynamo.enhancedExp

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.core.Kdsl
import net.kotlinx.string.lett


/**
 * 자주 사용되는 쿼리 표현식 샘플
 * 사실 DDB 에서 이거 이상으로 더 나올게 없긴 함
 * */
object DbExpressionSet {


    /** 빈 스캔. 페이징 때문에 기본 템플릿은 있어야함  */
    class None : DbExpression {

        @Kdsl
        constructor(block: None.() -> Unit = {}) {
            apply(block)
        }

        override fun filterExpression(): String? = null
        override fun expressionAttributeValues(): Map<String, AttributeValue>? = null
    }


    /** PK 접두사로 스캐닝 (쿼리는 이거 불가능) */
    class PkPrefix : DbExpression {

        @Kdsl
        constructor(block: PkPrefix.() -> Unit = {}) {
            apply(block)
        }

        override fun filterExpression(): String? = "begins_with (${pkName}, :${pkName})"

        override fun expressionAttributeValues(): Map<String, AttributeValue> = mapOf(
            ":${pkName}" to AttributeValue.S(pk)
        )

    }

    /**
     * PK & SK 동일한거
     * SK 가 있을경우 SK도 비교해줌 ( 인덱스는 SK 동일하게 여러개 넣을 수 있음)
     *  */
    class PkSkEq : DbExpression {

        @Kdsl
        constructor(block: PkSkEq.() -> Unit = {}) {
            apply(block)
        }

        override fun keyConditionExpression(): String =
            if (sk.isNullOrEmpty()) "${pkName} = :${pkName}" else "${pkName} = :${pkName} AND ${skName} = :${skName}"

        override fun expressionAttributeValues(): Map<String, AttributeValue> = buildMap {
            put(":${pkName}", AttributeValue.S(pk))
            sk.lett { put(":${skName}", AttributeValue.S(it)) }
        }

    }

    /** SK 접두사 */
    class SkPrefix : DbExpression {

        @Kdsl
        constructor(block: SkPrefix.() -> Unit = {}) {
            apply(block)
        }

        override fun keyConditionExpression(): String = "${pkName} = :${pkName} AND begins_with (${skName}, :${skName})"

        override fun expressionAttributeValues(): Map<String, AttributeValue> = mapOf(
            ":${pkName}" to AttributeValue.S(pk),
            ":${skName}" to AttributeValue.S(sk!!),
        )

    }

    /** SK 접두사 여러개 */
    class SkPrefixIn : DbExpression {

        lateinit var dd: String

        @Kdsl
        constructor(block: SkPrefixIn.() -> Unit = {}) {
            apply(block)
        }

        override fun keyConditionExpression(): String =
            "${pkName} = :${pkName} AND (begins_with (${skName}, :${skName}) or begins_with (${skName}, :dd ) )   "

        override fun expressionAttributeValues(): Map<String, AttributeValue> = mapOf(
            ":${pkName}" to AttributeValue.S(pk),
            ":${skName}" to AttributeValue.S(sk!!),
            ":dd" to AttributeValue.S(dd!!),
        )

    }


}