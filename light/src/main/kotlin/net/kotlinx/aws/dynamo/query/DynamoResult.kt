package net.kotlinx.aws.dynamo.query

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue

/**
 * 페이징용 DDB 결과 래퍼
 * @see DynamoResp 과는 다르게 map이 아니라 T 를 매핑한다
 * */
data class DynamoResult<T>(val datas: List<T>, val lastEvaluatedKey: Map<String, AttributeValue>? = null)