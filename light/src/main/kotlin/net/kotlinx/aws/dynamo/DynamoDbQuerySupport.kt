package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.batchGetItem
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.KeysAndAttributes
import aws.sdk.kotlin.services.dynamodb.model.Select
import aws.sdk.kotlin.services.dynamodb.paginators.queryPaginated

//==================================================== 단일 쿼리 ======================================================

suspend fun <T : DynamoData> DynamoDbClient.query(data: T,block: DynamoQuery.() -> Unit = {}): List<T> {
    val query = DynamoQuery(block)
    val req = query.toQueryRequest(data)
    val firstScan = this.query(req).items!!
    if (firstScan.isEmpty()) return emptyList()
    //부분 데이터만 가지고있을경우(보통 인덱스) 다시 조회 해준다
    val items = when (query.select) {
        Select.AllProjectedAttributes -> batchGetItem(data.tableName, firstScan)
        else -> firstScan
    }
    return items.map { data.fromAttributeMap(it) as T }
}


/** 키값 단위로 1:1 매핑해서 가져옴 */
suspend fun DynamoDbClient.batchGetItem(tableName: String, params: List<Map<String, AttributeValue>>): List<Map<String, AttributeValue>> = this.batchGetItem {
    this.requestItems = mapOf(
        tableName to KeysAndAttributes {
            this.keys = params.map {
                mapOf(
                    DynamoDbBasic.PK to it[DynamoDbBasic.PK]!!,
                    DynamoDbBasic.SK to it[DynamoDbBasic.SK]!!,
                )
            }
        }
    )
}.responses!!.values.flatten()


//==================================================== 페이징 쿼리 ======================================================

/** 청크 단위 처리. (Flow는 멈추는거 불가능) */
suspend fun <T : DynamoData> DynamoDbClient.queryAll(query: DynamoQuery, data: T, action: suspend (List<T>) -> Unit) {
    val paginated = this.queryPaginated(query.toQueryRequest(data))
    //처리 단위가 달라서 컬렉트 리스트는 쓰지않음
    paginated.collect { v ->
        val firstScan = v.items!!
        if (firstScan.isEmpty()) return@collect

        val itemMaps: List<Map<String, AttributeValue>> = when (query.select) {
            Select.AllProjectedAttributes -> this.batchGetItem(data.tableName, firstScan)
            else -> firstScan
        }
        val items = itemMaps.map { data.fromAttributeMap(it) as T }
        action(items)
    }
}

/** 전체 로드 (메모리 주의) */
suspend fun <T : DynamoData> DynamoDbClient.queryAll(query: DynamoQuery, data: T): List<T> =
    mutableListOf<T>().also { list -> this.queryAll(query, data) { list.addAll(it) } }.toList()

