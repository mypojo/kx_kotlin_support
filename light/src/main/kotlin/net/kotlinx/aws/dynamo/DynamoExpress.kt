package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue


/**
 * 쿼리 표현식 샘플
 * */
interface DynamoExpress {
    /**
     * keyConditionExpression (쿼리)
     * filterExpression (스캔)
     * */
    fun expression(): String

    /**
     * expression의 실제 값
     * */
    fun expressionAttributeValues(): Map<String, AttributeValue>
}