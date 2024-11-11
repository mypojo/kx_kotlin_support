@file:Suppress("DuplicatedCode")

package net.kotlinx.aws.ddb

//==================================================== 단일 쿼리 ======================================================

///** 고정된 쿼리 사용 */
//suspend fun <T : DynamoData> DynamoDbClient.query(query: DynamoQuery, data: T): DynamoResult<T> {
//    val req = query.toQueryRequest(data.tableName)
//    val resp = this.query(req)
//    val firstQuery = resp.items!!
//    if (firstQuery.isEmpty()) return DynamoResult(emptyList(), resp.lastEvaluatedKey)
//
//    //부분 데이터만 가지고있을경우(보통 인덱스) 다시 조회 해준다. 이때 정렬이 풀림으로 재정렬 해줘야함
//    val itemMaps = when (query.select) {
//        Select.AllProjectedAttributes -> batchGetItem(data.tableName, firstQuery)
//        else -> firstQuery
//    }
//    val items = itemMaps.map { data.fromAttributeMap(it) as T }
//    return DynamoResult(items, resp.lastEvaluatedKey)
//}
//
///**
// * 런타임에 쿼리 설정을 재정의해서 쓸때 사용
// * @param data 이게 없으면 더미 넣자.
// *  */
//suspend fun <T : DynamoData> DynamoDbClient.query(data: T, block: DynamoQuery.() -> Unit = {}): DynamoResult<T> = query(DynamoQuery(block), data)
//
///** batchGetItem 의 DynamoData 버전 */
//suspend fun <T : DynamoData> DynamoDbClient.batchGetItem(items: List<T>): List<T> {
//    if (items.isEmpty()) return emptyList()
//    val item = items.first()
//    val tableName = item.tableName
//    return this.batchGetItem(tableName, items.map { it.toKeyMap() }).map { item.fromAttributeMap(it) }
//}
