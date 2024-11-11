package net.kotlinx.aws.ddb

import aws.sdk.kotlin.services.dynamodb.*
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.KeysAndAttributes
import aws.sdk.kotlin.services.dynamodb.model.ReturnValue
import net.kotlinx.aws.ddb.exp.DbResult

/** 입력 */
suspend fun <T : DbItem> DynamoDbClient.put(item: T) {
    val table = item.table()
    table.beforePut(item)
    if (!table.persist(item)) return
    this.putItem {
        this.tableName = table.tableName
        this.item = table.converter.toAttribute(item)
    }
}

/** 수정 */
suspend fun <T : DbItem> DynamoDbClient.update(item: T, updateKeys: Collection<String>) {
    val table = item.table()
    if (!table.persist(item)) return
    this.updateItem {
        this.tableName = table.tableName
        this.key = item.toKeyMap()
        this.updateExpression = "SET ${updateKeys.joinToString(",") { "$it = :${it}" }}"
        this.expressionAttributeValues = table.converter.toAttribute(item).filterKeys { it in updateKeys }.mapKeys { ":${it.key}" }
        this.returnValues = ReturnValue.AllNew
    }
}

/** 조회 */
suspend fun <T : DbItem> DynamoDbClient.get(item: T): T? {
    val table = item.table()
    val map: Map<String, AttributeValue> = this.getItem {
        this.tableName = table.tableName
        this.consistentRead = false
        this.key = item.toKeyMap()
    }.item ?: return null
    return table.converter.fromAttributeMap(map)
}


/** 삭제 */
suspend fun <T : DbItem> DynamoDbClient.delete(item: T, returnValue: ReturnValue = ReturnValue.None) {
    val table = item.table()
    this.deleteItem {
        this.tableName = table.tableName
        this.key = item.toKeyMap()
        this.returnValues = returnValue
    }
}


/**
 * batchGetItem 의 로우 API 버전
 * query시 본문을 채워줄때도 사용됨
 * 재정렬 작업을 추가해준다.
 *  */
suspend fun DynamoDbClient.getBatch(table: DbTable, params: List<Map<String, AttributeValue>>): DbResult {
    val orders = params.map { it[table.skName]!! } //정렬정보 저장
    val results = this.batchGetItem {
        this.requestItems = mapOf(
            table.tableName to KeysAndAttributes {
                this.keys = params.map {
                    mapOf(
                        table.pkName to it[table.pkName]!!,
                        table.skName to it[table.skName]!!,
                    )
                }
            }
        )
    }.responses!!.values.flatten()

    //원래 순서로 재졍렬
    val groupBySk = results.associateBy { it[table.skName]!! }
    val itemMaps = orders.map { sk -> groupBySk[sk]!! }
    return DbResult(table, itemMaps, null)
}

