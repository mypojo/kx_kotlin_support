package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.AwsConfig


/** 권장하는 키값 스타일 마킹 인터페이스 */
interface DynamoDbBasic {

    val pk: String
    val sk: String

    companion object {
        const val PK = "pk"
        const val SK = "sk"
    }
}

/**
 * 제너릭 때문에 이 클래스를 끝까지 유지 해야함
 *
 *  */
interface DynamoData : DynamoDbBasic {

    val tableName: String

    fun toKeyMap(): Map<String, AttributeValue> {
        return mapOf(
            DynamoDbBasic.PK to AttributeValue.S(this.pk),
            DynamoDbBasic.SK to AttributeValue.S(this.sk),
        )
    }

    /** 간단 PK 확인용 ex) 로깅 */
    fun toKeyString(): String = "${pk}:${sk}"

    //==================================================== 개별 구현 ======================================================

    /** 이건 객체마다 해줘야함 */
    fun toAttribute(): Map<String, AttributeValue>

    /**
     * 이건 객체마다 해줘야함
     * companion에 있는게 더 적절하긴함
     *  */
    fun <T : DynamoData> fromAttributeMap(map: Map<String, AttributeValue>): T

    /** 콘솔링크 */
    fun toConsoleLink(region: String = AwsConfig.SEOUL): String = DynamoUtil.toConsoleLink(tableName, this, region)
}

