package net.kotlinx.aws.ddb

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.dynamo.DynamoBasic


/** 권장하는 키값 스타일 마킹 인터페이스 */
interface DdbData {

    val pk: String
    val sk: String

    fun toKeyMap(): Map<String, AttributeValue> {
        return mapOf(
            DynamoBasic.PK to AttributeValue.S(this.pk),
            DynamoBasic.SK to AttributeValue.S(this.sk),
        )
    }

    /** 간단 PK 확인용 ex) 로깅 */
    fun toKeyString(): String = "${pk}:${sk}"

}
