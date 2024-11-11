package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.AwsConfig

/**
 * 제너릭 때문에 이 클래스를 끝까지 유지 해야함
 *  */
@Deprecated("쓰지마셈")
interface DynamoData : DynamoBasic {

    val tableName: String

    fun toKeyMap(): Map<String, AttributeValue> {
        return mapOf(
            DynamoBasic.PK to AttributeValue.S(this.pk),
            DynamoBasic.SK to AttributeValue.S(this.sk),
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
    fun toConsoleLink(region: String = AwsConfig.REGION_KR): String = DynamoUtil.toConsoleLink(tableName, this, region)
}