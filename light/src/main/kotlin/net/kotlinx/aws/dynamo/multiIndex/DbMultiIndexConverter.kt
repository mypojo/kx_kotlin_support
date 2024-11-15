package net.kotlinx.aws.dynamo.multiIndex

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.dynamo.add
import net.kotlinx.aws.dynamo.enhanced.DbConverter
import net.kotlinx.aws.dynamo.enhanced.DbItem
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.aws.dynamo.find
import net.kotlinx.aws.dynamo.findOrThrow
import net.kotlinx.domain.job.Job

/**
 * DDB에 입력되는 메타데이터
 * https://www.notion.so/mypojo/Job-Module-serverless-docker-57e773b5f0494fb59dcbff5d9a8eb8f5
 */
class DbMultiIndexConverter(private val table: DbTable) : DbConverter {

    override fun <T : DbItem> toAttribute(data: T): Map<String, AttributeValue> {
        val item = data as DbMultiIndexItem
        return buildMap {
            this += DbTable.PK_NAME to AttributeValue.S(item.pk)
            this += DbTable.SK_NAME to AttributeValue.S(item.sk)

            //==================================================== 최초 생성시 필수 입력값 ======================================================
            add(DbMultiIndexItem::ttl, item.ttl)
            add(DbMultiIndexItem::body, item.body)

            //==================================================== 공통 시스템 자동(필수) 입력값 ======================================================
            add(DbMultiIndex.GSI01, item.gsi01)
            add(DbMultiIndex.GSI02, item.gsi02)
            add(DbMultiIndex.GSI03, item.gsi03)
            add(DbMultiIndex.GSI04, item.gsi04)
        }
    }

    override fun <T : DbItem> fromAttributeMap(map: Map<String, AttributeValue>): T {
        return DbMultiIndexItem(
            map[table.pkName]!!.asS(), map[table.skName]!!.asS()
        ).apply {
            //==================================================== 최초 생성시 필수 입력값 ======================================================
            body = map.findOrThrow(DbMultiIndexItem::body)
            ttl = map.find(Job::ttl)

            //==================================================== 공통 시스템 자동(필수) 입력값 ======================================================
            gsi01 = map.findPair(DbMultiIndex.GSI01)
            gsi02 = map.findPair(DbMultiIndex.GSI02)
            gsi03 = map.findPair(DbMultiIndex.GSI03)
            gsi04 = map.findPair(DbMultiIndex.GSI04)
        } as T
    }


}