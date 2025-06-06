package net.kotlinx.aws.dynamo.enhanced

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue


/**
 * DDB를 객체로 변환
 * Enhanced 매퍼 안씀!
 * */
interface DbConverter<T : DbItem> {

    /** 객체를 map으로 변경 */
    fun toAttribute(data: T): Map<String, AttributeValue>

    /** map을 객체로 변경 */
    fun fromAttributeMap(map: Map<String, AttributeValue>): T

}

