package net.kotlinx.aws.dynamo.enhanced

import aws.sdk.kotlin.services.dynamodb.*
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.KeysAndAttributes
import aws.sdk.kotlin.services.dynamodb.model.ReturnValue
import net.kotlinx.aws.dynamo.enhancedExp.DbExpression
import net.kotlinx.aws.dynamo.enhancedExp.DbResult

//==================================================== 기본로직 ======================================================

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

//==================================================== 커스텀 ======================================================

/** 벌크 로드 */
suspend fun <T : DbItem> DynamoDbClient.getBatch(keyItems: List<T>): List<T> {
    if (keyItems.isEmpty()) return emptyList()

    val table = keyItems.first().table()
    val keyMaps = keyItems.map { it.toKeyMap() }
    val keyDatas = getBatch(table, keyMaps)
    return DbResult(table, keyDatas).datas()
}

/**
 * 로우 API 버전 -> 쿼리에 사용됨
 * query시 본문을 채워줄때도 사용됨
 * 재정렬 작업을 추가해준다.
 *  */
suspend fun DynamoDbClient.getBatch(table: DbTable, params: List<Map<String, AttributeValue>>): List<Map<String, AttributeValue>> {

    val orders = params.map { it[table.skName]!! } //정렬정보 저장

    val results = params.chunked(DbExpression.GET_LIMIT).flatMap { chunk ->
        this.batchGetItem {
            this.requestItems = mapOf(
                table.tableName to KeysAndAttributes {
                    this.keys = chunk.map {
                        mapOf(
                            table.pkName to it[table.pkName]!!,
                            table.skName to it[table.skName]!!,
                        )
                    }
                }
            )
        }.responses!!.values.flatten()
    }

    //원래 순서로 재졍렬
    val groupBySk = results.associateBy { it[table.skName]!! }
    return orders.map { sk -> groupBySk[sk]!! }
}

