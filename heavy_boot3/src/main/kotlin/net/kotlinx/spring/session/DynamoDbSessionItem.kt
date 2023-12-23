package net.kotlinx.spring.session

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.dynamo.DynamoData
import net.kotlinx.aws.dynamo.DynamoDbBasic
import net.kotlinx.aws.dynamo.findOrThrow

/** DDB에 입력하는 데이터  */
class DynamoDbSessionItem(
    override val sk: String,
) : DynamoData {

    var ttl: Long = 0
    lateinit var data: ByteArray

    override val tableName: String
        get() = sessionTableName

    override fun toAttribute(): Map<String, AttributeValue> {
        return mutableMapOf<String, AttributeValue>().apply {
            this += DynamoDbBasic.PK to AttributeValue.S(pk)
            this += DynamoDbBasic.SK to AttributeValue.S(sk)
            this += DynamoDbSessionItem::ttl.name to AttributeValue.N(ttl.toString())
            this += DynamoDbSessionItem::data.name to AttributeValue.B(data)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : DynamoData> fromAttributeMap(map: Map<String, AttributeValue>): T = DynamoDbSessionItem(map[DynamoDbBasic.SK]!!.asS()).apply {
        ttl = map.findOrThrow(DynamoDbSessionItem::ttl)
        data = map[DynamoDbSessionItem::data.name]!!.asB()
    } as T

    override val pk: String = DynamoDbBasic.PK

    companion object {
        lateinit var sessionTableName: String
    }

}