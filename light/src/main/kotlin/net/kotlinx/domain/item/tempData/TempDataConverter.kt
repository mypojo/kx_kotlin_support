package net.kotlinx.domain.item.tempData

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.dynamo.enhanced.DbConverter
import net.kotlinx.aws.dynamo.enhanced.DbItem
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.aws.dynamo.findOrThrow
import net.kotlinx.time.toIso

class TempDataConverter(private val table: DbTable) : DbConverter {

    override fun <T : DbItem> toAttribute(data: T): Map<String, AttributeValue> {
        val item = data as TempData
        return buildMap {
            put(table.pkName, AttributeValue.S(item.pk))
            put(table.skName, AttributeValue.S(item.sk))

            //==================================================== 최초 생성시 필수 입력값 ======================================================
            put(TempData::reqTime.name, AttributeValue.S(item.reqTime.toIso()))
            put(TempData::body.name, AttributeValue.S(item.body))
            put(TempData::ttl.name, AttributeValue.N(item.ttl.toString()))
        }
    }

    override fun <T : DbItem> fromAttributeMap(map: Map<String, AttributeValue>): T {
        return TempData(
            map[table.pkName]!!.asS(), map[table.skName]!!.asS()
        ).apply {
            //==================================================== 최초 생성시 필수 입력값 ======================================================
            reqTime = map.findOrThrow(TempData::reqTime)
            body = map.findOrThrow(TempData::body)
            ttl = map.findOrThrow(TempData::ttl)
        } as T
    }


}