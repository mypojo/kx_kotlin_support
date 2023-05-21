package net.kotlinx.kopring.spring.session

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws1.dynamo.DynamoData
import net.kotlinx.aws1.dynamo.DynamoDbBasic
import net.kotlinx.module1.reflect.findOrThrow

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
            this += DynamoDbBasic.pk to AttributeValue.S(pk)
            this += DynamoDbBasic.sk to AttributeValue.S(sk)
            this += DynamoDbSessionItem::ttl.name to AttributeValue.N(ttl.toString())
            this += DynamoDbSessionItem::data.name to AttributeValue.B(data)
        }
    }

    override fun <T : DynamoData> fromAttributeMap(map: Map<String, AttributeValue>): T = DynamoDbSessionItem(map[DynamoDbBasic.sk]!!.asS()).apply {
        ttl = map.findOrThrow(DynamoDbSessionItem::ttl)
        data = map[DynamoDbSessionItem::data.name]!!.asB()
    } as T

    override val pk: String = DynamoDbBasic.pk

    companion object {
        lateinit var sessionTableName: String
    }

}