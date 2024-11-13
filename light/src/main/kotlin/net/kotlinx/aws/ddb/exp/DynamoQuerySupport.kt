package net.kotlinx.aws.ddb.exp

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.Select
import net.kotlinx.aws.ddb.DbItem
import net.kotlinx.aws.ddb.getBatch
import net.kotlinx.collection.doUntilTokenNull

//==================================================== 단일 쿼리 ======================================================

/** 간단 쿼리 -> 단축 */
suspend fun DynamoDbClient.query(block: () -> DbExpression): DbResult = query(block())

/** 간단 쿼리 */
suspend fun DynamoDbClient.query(exp: DbExpression): DbResult {
    val resp = this.query(exp.toQueryRequest())
    val firstQuery = resp.items!!
    if (firstQuery.isEmpty()) return DbResult(exp.table, emptyList(), resp.lastEvaluatedKey)

    //부분 데이터만 가지고있을경우(보통 인덱스) 다시 조회 해준다. 이때 정렬이 풀림으로 재정렬 해줘야함
    val itemMaps = when (exp.select) {
        Select.AllProjectedAttributes -> getBatch(exp.table, firstQuery).maps
        else -> firstQuery
    }
    return DbResult(exp.table, itemMaps, resp.lastEvaluatedKey)
}


/** 전체 조회 -> 단축 */
suspend fun <T : DbItem> DynamoDbClient.queryAll(block: () -> DbExpression): List<T> = queryAll(block())

/** 전체 조회 */
suspend fun <T : DbItem> DynamoDbClient.queryAll(expression: DbExpression): List<T> {
    expression.limit = DbExpression.MAX_LIMIT //전부 가져올때는 강제로 최대치를 가져옴
    val itemMaps = doUntilTokenNull { _, last ->
        expression.exclusiveStartKey = last as Map<String, AttributeValue>?
        val result2 = query(expression)
        result2.maps to result2.lastEvaluatedKey
    }.flatten()
    return DbResult(expression.table, itemMaps, null).datas()
}
