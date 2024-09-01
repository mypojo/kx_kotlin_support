package net.kotlinx.lock

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.dynamo.DynamoBasic
import net.kotlinx.aws.dynamo.DynamoData
import net.kotlinx.aws.dynamo.findOrThrow
import net.kotlinx.json.gson.GsonData
import net.kotlinx.lazyLoad.LazyLatchProperty

/** 락 잡힌 리소스 */
class ResourceItem(override val pk: String, override val sk: String) : DynamoData {

    override val tableName: String
        get() = TABLE_NAME

    override fun toAttribute(): Map<String, AttributeValue> {
        return mutableMapOf<String, AttributeValue>().apply {
            this += DynamoBasic.PK to AttributeValue.S(pk)
            this += DynamoBasic.SK to AttributeValue.S(sk)

            //==================================================== 최초 생성시 필수 입력값 ======================================================
            this += ResourceItem::inUse.name to AttributeValue.Bool(inUse)
            this += ResourceItem::ttl.name to AttributeValue.N(ttl.toString())
            this += ResourceItem::body.name to AttributeValue.S(body.toString())
            this += ResourceItem::div.name to AttributeValue.S(div)
            this += ResourceItem::cause.name to AttributeValue.S(cause)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : DynamoData> fromAttributeMap(map: Map<String, AttributeValue>): T = ResourceItem(
        map[DynamoBasic.PK]!!.asS(), map[DynamoBasic.SK]!!.asS()
    ).apply {
        //==================================================== 최초 생성시 필수 입력값 ======================================================
        inUse = map.findOrThrow(ResourceItem::inUse)
        ttl = map.findOrThrow(ResourceItem::ttl)
        body = map.findOrThrow(ResourceItem::body)
        div = map.findOrThrow(ResourceItem::div)
        cause = map.findOrThrow(ResourceItem::cause)

    } as T

    /** 락이 사용중인지 여부 */
    var inUse: Boolean = false

    /**
     * TTL
     * 보통 작업에 2시간 걸린다면
     * 실제 TTL에서 2시간을 빼고 입력해야 안전하게 리소스 사용이 가능하다.
     *  */
    var ttl: Long = 0

    /** 전달할 내용 */
    lateinit var body: GsonData

    /**
     * 락 구분값.
     * ex) job 이름
     *  */
    var div: String = ""

    /**
     * 락 사유 등의 코멘트
     *  */
    var cause: String = ""

    companion object {
        /** 테이블 이름을 여기서 지정 (한번만 지정 가능) */
        var TABLE_NAME: String by LazyLatchProperty()
    }


}