package net.kotlinx.aws.dynamo.query

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue

/**
 *  다이나모 DB 응답객체
 *  중간중간 변환이 들어가기 때문에 별도로 뺐다
 * */
data class DynamoResp(val datas: List<Map<String, AttributeValue>>, val lastEvaluatedKey: Map<String, AttributeValue>? = null)