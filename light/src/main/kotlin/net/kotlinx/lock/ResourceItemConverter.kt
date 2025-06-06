package net.kotlinx.lock

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.dynamo.enhanced.DbConverter
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.aws.dynamo.findOrThrow

/**
 * DDB에 입력되는 메타데이터
 * 주의!! 향후 신규 작업시 ErrorLogConverter 참고해서 코드 리팩토링 하기!
 */
class ResourceItemConverter(private val table: DbTable) : DbConverter<ResourceItem> {

    override fun toAttribute(item: ResourceItem): Map<String, AttributeValue> {
        return buildMap {
            put(table.pkName, AttributeValue.S(item.pk))
            put(table.skName, AttributeValue.S(item.sk))
            //==================================================== 최초 생성시 필수 입력값 ======================================================
            put(ResourceItem::inUse.name, AttributeValue.Bool(item.inUse))
            put(ResourceItem::ttl.name, AttributeValue.N(item.ttl.toString()))
            put(ResourceItem::body.name, AttributeValue.S(item.body.toString()))
            put(ResourceItem::div.name, AttributeValue.S(item.div))
            put(ResourceItem::cause.name, AttributeValue.S(item.cause))
        }
    }

    override fun fromAttributeMap(map: Map<String, AttributeValue>): ResourceItem {
        return ResourceItem(
            map[table.pkName]!!.asS(), map[table.skName]!!.asS()
        ).apply {
            inUse = map.findOrThrow(ResourceItem::inUse)
            ttl = map.findOrThrow(ResourceItem::ttl)
            body = map.findOrThrow(ResourceItem::body)
            div = map.findOrThrow(ResourceItem::div)
            cause = map.findOrThrow(ResourceItem::cause)
        }
    }


}