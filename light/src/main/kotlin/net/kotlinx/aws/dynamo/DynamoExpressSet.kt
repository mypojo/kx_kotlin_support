package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue


/**
 * 쿼리 표현식 샘플
 * */
object DynamoExpressSet {


    object Scan {

        /** PK 접두사로 스캐닝 (쿼리는 이거 불가능) */
        class DynamoExpressPkPrefix(private val pkPrefix: String) : DynamoExpress {

            override fun expression(): String = "begins_with (pk, :pkPrefix)"

            override fun expressionAttributeValues(): Map<String, AttributeValue> = mapOf(
                ":pkPrefix" to AttributeValue.S(pkPrefix)
            )

        }

    }

    object Query {

        /** SK 접두사로 스캐닝 with Index */
        class DynamoExpressSkPrefix(val indexName: String, private val skName: String, val pk: String, val skPrefix: String) : DynamoExpress {

            override fun expression(): String = "pk = :pk AND begins_with (${skName}, :skPrefix)"

            override fun expressionAttributeValues(): Map<String, AttributeValue> = mapOf(
                ":${DynamoDbBasic.PK}" to AttributeValue.S(pk),
                ":skPrefix" to AttributeValue.S(skPrefix),
            )

        }

    }


}