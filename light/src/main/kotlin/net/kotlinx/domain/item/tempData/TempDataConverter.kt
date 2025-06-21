package net.kotlinx.domain.item.tempData

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.dynamo.enhanced.DbConverter
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.aws.dynamo.findOrThrow
import net.kotlinx.aws.dynamo.put

class TempDataConverter(private val table: DbTable) : DbConverter<TempData> {

    override fun toAttribute(item: TempData): Map<String, AttributeValue> {
        return buildMap {
            put(table.pkName, item.pk)
            put(table.skName, item.sk)
            put(TempData::status.name, item.status)
            put(TempData::regTime.name, item.regTime)
            put(TempData::body.name, item.body)
            put(TempData::ttl.name, item.ttl)
        }
    }

    override fun fromAttributeMap(map: Map<String, AttributeValue>): TempData {
        return TempData(
            pk = map[table.pkName]!!.asS(),
            sk = map[table.skName]!!.asS(),
            status = map.findOrThrow(TempData::status),
            body = map.findOrThrow(TempData::body),
            regTime = map.findOrThrow(TempData::regTime),
            ttl = map.findOrThrow(TempData::ttl),
        )
    }


}