package net.kotlinx.aws1.dynamo

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.deleteItem
import aws.sdk.kotlin.services.dynamodb.getItem
import aws.sdk.kotlin.services.dynamodb.model.*
import aws.sdk.kotlin.services.dynamodb.paginators.queryPaginated
import aws.sdk.kotlin.services.dynamodb.putItem
import aws.sdk.kotlin.services.dynamodb.updateItem
import kotlinx.coroutines.flow.Flow

//==================================================== 트랜잭션 ======================================================
//데이터 타입은 아래 문서 참고
//https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.NamingRulesDataTypes.html#HowItWorks.DataTypes
//update 참고
//https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.UpdateExpressions.html#Expressions.UpdateExpressions.ADD

/** 해당 로우의 특정 컬럼값에 synch하게  +1 후 리턴 */
suspend fun DynamoDbClient.increaseAndGet(incTableName: String, pkName: String, pkValue: String, columnName: String): Long {
    val resp = this.updateItem {
        this.tableName = incTableName
        this.updateExpression = "set $columnName = $columnName + :val"
        this.expressionAttributeValues = mapOf(":val" to AttributeValue.N("1")) //1씩 늘림
        this.key = mapOf(pkName to AttributeValue.S(pkValue))
        this.returnValues = ReturnValue.AllNew //새 값 리턴
    }
    return resp.attributes!![columnName]!!.asN().toLong()
}

/**
 * 해당 로우의 특정 map type(M – Map) 컬럼값에 synch하게  add(오버라이드) 후 리턴하는 샘플
 * 1. 기존 map에 데이터가 많아도, 추가만 수행한다.
 * 2. 해당 컬럼에 map이 없으면 작동하지 않는다. 빈 map 이라도 반드시 존재해야 한다
 * */
suspend fun DynamoDbClient.updateItemMap(tableName: String, pk: String, sk: String, columnName: String, append: Map<String, String>) {
    this.updateItem {
        this.tableName = tableName
        this.returnValues = ReturnValue.None //필요한 경우 없음.
        this.key = mapOf(
            DynamoDbBasic.pk to AttributeValue.S(pk),
            DynamoDbBasic.sk to AttributeValue.S(sk),
        )
        this.updateExpression = "set " + append.entries.joinToString(",") { "${columnName}.${it.key} = :${it.key}" }
        this.expressionAttributeValues = append.map { ":${it.key}" to AttributeValue.S(it.value) }.toMap()
    }
}

//==================================================== 단일 ======================================================

/** 결과를 별도로 파싱하지 않음 */
suspend fun DynamoDbClient.putItem(data: DynamoData): PutItemResponse = this.putItem {
    this.tableName = data.tableName
    this.item = data.toAttribute()
}

/** 간단 업데이트.  List<KProperty<*>> 사용할것 */
suspend fun DynamoDbClient.updateItem(data: DynamoData, updateKeys: List<String>): UpdateItemResponse {
    return this.updateItem {
        this.tableName = data.tableName
        this.key = data.toKeyMap()
        this.updateExpression = "SET ${updateKeys.joinToString(",") { "$it = :${it}" }}"
        this.expressionAttributeValues = data.toAttribute().filterKeys { it in updateKeys }.mapKeys { ":${it.key}" }
        this.returnValues = ReturnValue.AllNew
    }
}

/** 간단 삭제 */
suspend fun DynamoDbClient.deleteItem(data: DynamoData, returnValue: ReturnValue = ReturnValue.None): DeleteItemResponse = this.deleteItem {
    this.tableName = data.tableName
    this.key = data.toKeyMap()
    this.returnValues = returnValue
}

suspend fun <T : DynamoData> DynamoDbClient.getItem(data: T): T? {
    val map: Map<String, AttributeValue> = this.getItem {
        this.tableName = data.tableName
        this.consistentRead = false
        this.key = data.toKeyMap()
    }.item ?: return null
    return data.fromAttributeMap(map)
}


//==================================================== 쿼리 ======================================================

/** 한번 조회 */
suspend fun <T : DynamoData> DynamoDbClient.query(data: T, query: DynamoQuery): List<T> = this.query(query.toQueryRequest(data)).items!!.map { data.fromAttributeMap(it) }

/** Flow를 일괄 처리해준다. */
suspend fun <T> Flow<T>.readAll(action: suspend (v: T) -> Unit) = this.collect { action.invoke(it) }

/** 전체 조회 */
suspend fun <T : DynamoData> DynamoDbClient.queryAll(data: T, query: DynamoQuery, action: suspend (v: List<T>) -> Unit) {
    //원하는 형태로 한번 더 래핑해줌
    this.queryPaginated(query.toQueryRequest(data)).readAll { resp ->
        val datas = resp.items!!.map { data.fromAttributeMap(it) as T }
        action.invoke(datas)
    }
}

/** 전체 조회 */
suspend fun <T : DynamoData> DynamoDbClient.queryAll(data: T, query: DynamoQuery): List<T> = mutableListOf<T>().also { list -> queryAll(data, query) { list.addAll(it) } }.toList()