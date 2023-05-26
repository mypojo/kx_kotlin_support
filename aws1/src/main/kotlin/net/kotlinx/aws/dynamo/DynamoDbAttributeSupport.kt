package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue

/** map을 AttributeValue.M 으로 변환해줌 */
fun Map<String, String>.toDynamoAttribute(): AttributeValue = AttributeValue.M(this.map { it.key to AttributeValue.S(it.value) }.toMap())