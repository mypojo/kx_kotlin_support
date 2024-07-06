package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue

/**
 * 페이징용 DDB 결과 래퍼
 * */
data class DynamoResult<T>(val datas: List<T>, val lastEvaluatedKey: Map<String, AttributeValue>? = null)