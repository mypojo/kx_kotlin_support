package net.kotlinx.aws.ddb

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue


/**
 * DDB를 객체로 변환
 * Enhanced 매퍼 안씀!
 * */
interface DbConverter {

    /** 객체를 map으로 변경 */
    fun <T : DbItem> toAttribute(data: T): Map<String, AttributeValue>

    /** map을 객체로 변경 */
    fun <T : DbItem> fromAttributeMap(map: Map<String, AttributeValue>): T

}

