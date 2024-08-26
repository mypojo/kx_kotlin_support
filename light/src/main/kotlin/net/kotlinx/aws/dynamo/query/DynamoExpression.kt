package net.kotlinx.aws.dynamo.query

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.dynamo.DynamoDbBasic


/**
 * 쿼리 표현식 샘플
 * */
abstract class DynamoExpression {

    var pkName: String = DynamoDbBasic.PK
    var skName: String = DynamoDbBasic.SK

    /** PK 값 */
    lateinit var pk: String

    /** SK 값 */
    var sk: String? = null

    /**
     * keyConditionExpression (쿼리)
     * filterExpression (스캔)
     * */
    abstract fun expression(): String

    /**
     * expression의 실제 값
     * */
    abstract fun expressionAttributeValues(): Map<String, AttributeValue>
}