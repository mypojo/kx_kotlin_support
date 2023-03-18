package net.kotlinx.aws1.dynamo

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.getItem
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.PutItemResponse
import aws.sdk.kotlin.services.dynamodb.model.ReturnValue
import aws.sdk.kotlin.services.dynamodb.model.UpdateItemResponse
import aws.sdk.kotlin.services.dynamodb.paginators.queryPaginated
import aws.sdk.kotlin.services.dynamodb.putItem
import aws.sdk.kotlin.services.dynamodb.updateItem
import kotlinx.coroutines.flow.Flow

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

suspend fun <T : DynamoData> DynamoDbClient.getItem(data: T): T {
    val map: Map<String, AttributeValue> = this.getItem {
        this.tableName = data.tableName
        this.consistentRead = false
        this.key = data.toKeyMap()
    }.item!!
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