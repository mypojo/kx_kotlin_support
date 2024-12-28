package net.kotlinx.spring.session//package net.kotlinx.spring.session
//
//import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
//import net.kotlinx.aws.dynamo.DynamoBasic
//import net.kotlinx.aws.dynamo.DynamoData
//import net.kotlinx.aws.dynamo.findOrThrow
//
///** DDB에 입력하는 데이터  */
//class DynamoSessionItem(
//    override val sk: String,
//) : DynamoData {
//
//    var ttl: Long = 0
//    lateinit var data: ByteArray
//
//    override val tableName: String
//        get() = sessionTableName
//
//    override fun toAttribute(): Map<String, AttributeValue> {
//        return mutableMapOf<String, AttributeValue>().apply {
//            this += DbTable.PK_NAME to AttributeValue.S(pk)
//            this += DbTable.SK_NAME to AttributeValue.S(sk)
//            this += DynamoSessionItem::ttl.name to AttributeValue.N(ttl.toString())
//            this += DynamoSessionItem::data.name to AttributeValue.B(data)
//        }
//    }
//
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : DynamoData> fromAttributeMap(map: Map<String, AttributeValue>): T = DynamoSessionItem(map[DbTable.SK_NAME]!!.asS()).apply {
//        ttl = map.findOrThrow(DynamoSessionItem::ttl)
//        data = map[DynamoSessionItem::data.name]!!.asB()
//    } as T
//
//    override val pk: String = DbTable.PK_NAME
//
//    companion object {
//        lateinit var sessionTableName: String
//    }
//
//}