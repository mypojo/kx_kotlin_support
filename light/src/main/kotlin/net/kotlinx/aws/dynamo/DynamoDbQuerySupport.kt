package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.batchGetItem
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.KeysAndAttributes
import aws.sdk.kotlin.services.dynamodb.model.Select
import aws.sdk.kotlin.services.dynamodb.paginators.queryPaginated

//==================================================== 단일 쿼리 ======================================================

/** 고정된 쿼리 사용 */
suspend fun <T : DynamoData> DynamoDbClient.query(query: DynamoQuery, data: T): DynamoResult<T> {
    val req = query.toQueryRequest(data)
    val resp = this.query(req)
    val firstQuery = resp.items!!
    if (firstQuery.isEmpty()) return DynamoResult(emptyList(), resp.lastEvaluatedKey)

    //부분 데이터만 가지고있을경우(보통 인덱스) 다시 조회 해준다. 이때 정렬이 풀림으로 재정렬 해줘야함
    val itemMaps = when (query.select) {
        Select.AllProjectedAttributes -> batchGetItem(data.tableName, firstQuery)
        else -> firstQuery
    }
    val items = itemMaps.map { data.fromAttributeMap(it) as T }
    return DynamoResult(items, resp.lastEvaluatedKey)
}

/**
 * 런타임에 쿼리 설정을 재정의해서 쓸때 사용
 * @param data 이게 없으면 더미 넣자.
 *  */
suspend fun <T : DynamoData> DynamoDbClient.query(data: T, block: DynamoQuery.() -> Unit = {}): DynamoResult<T> = query(DynamoQuery(block), data)

/**
 * batchGetItem 의 로우 API 버전
 * 재정렬 작업을 추가해준다.
 *  */
suspend fun DynamoDbClient.batchGetItem(tableName: String, params: List<Map<String, AttributeValue>>): List<Map<String, AttributeValue>> {
    val orders = params.map { it[DynamoDbBasic.SK]!! }
    val results = this.batchGetItem {
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

    val groupBySk = results.associateBy { it[DynamoDbBasic.SK]!! }
    return orders.map { sk -> groupBySk[sk]!! }
}


/** batchGetItem 의 DynamoData 버전 */
suspend fun <T : DynamoData> DynamoDbClient.batchGetItem(items: List<T>): List<T> {
    if (items.isEmpty()) return emptyList()
    val item = items.first()
    val tableName = item.tableName
    return this.batchGetItem(tableName, items.map { it.toKeyMap() }).map { item.fromAttributeMap(it) }
}


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

/**
 * 전체 로드 (메모리 주의)
 * 아마 내부적으로는 연속 페이징하고 동일할듯?
 *  */
suspend fun <T : DynamoData> DynamoDbClient.queryAll(query: DynamoQuery, data: T): List<T> =
    mutableListOf<T>().also { list -> this.queryAll(query, data) { list.addAll(it) } }.toList()

