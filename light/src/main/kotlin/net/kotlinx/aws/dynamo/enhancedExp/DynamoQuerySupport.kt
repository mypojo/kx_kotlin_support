package net.kotlinx.aws.dynamo.enhancedExp

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.Select
import aws.sdk.kotlin.services.dynamodb.paginators.queryPaginated
import kotlinx.coroutines.flow.*
import net.kotlinx.aws.dynamo.enhanced.DbItem
import net.kotlinx.aws.dynamo.enhanced.getBatch
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
        Select.AllProjectedAttributes -> getBatch(exp.table, firstQuery)
        else -> firstQuery
    }
    return DbResult(exp.table, itemMaps, resp.lastEvaluatedKey)
}

/**
 * 숫자만 리턴 (최적화)
 * 카운팅만 하면 네트워크 비용은 줄어들지만 스캔 비용은 여전하다!!
 *  -> 스캔 비용을 줄이기 위해서 인덱스를 가볍게 달것!!!
 * 스캔시 용량도 여전하기때문에 페이징도 적용해야함
 *  */
suspend fun DynamoDbClient.queryCnt(block: () -> DbExpression): Int {
    val expression = block().apply {
        //기본 세팅을 추가로 넣어준다. 어차피 리턴 형식이 달라서 제너릭도 불가능
        select = Select.Count
        limit = DbExpression.EXPRESSION_LIMIT
    }
    return this.queryPaginated(expression.toQueryRequest()).map { it.count }.toList().sum()

}

/** 전체 조회 */
@Deprecated("사용안함")
suspend fun <T : DbItem> DynamoDbClient.queryAll2(expression: DbExpression): List<T> {
    expression.limit = DbExpression.EXPRESSION_LIMIT //전부 가져올때는 강제로 최대치를 가져옴
    val itemMaps = doUntilTokenNull { _, last ->
        expression.exclusiveStartKey = last as Map<String, AttributeValue>?
        val result2 = query(expression)
        result2.maps to result2.lastEvaluatedKey
    }.flatten()
    return DbResult(expression.table, itemMaps, null).datas()
}

/** 전체 조회 -> 단축 */
fun <T : DbItem> DynamoDbClient.queryAll(block: () -> DbExpression): Flow<T> = queryAll(block())

/**
 * 전체 조회
 * AWS SDK의 페이징 함수를 이용해 Flow를 반환
 */
fun <T : DbItem> DynamoDbClient.queryAll(expression: DbExpression): Flow<T> {
    expression.limit = DbExpression.EXPRESSION_LIMIT // 전부 가져올때는 강제로 최대치를 가져옴
    return this.queryPaginated(expression.toQueryRequest()).flatMapConcat { response ->
        // 부분 데이터만 가지고있을경우(보통 인덱스) 다시 조회 해준다
        val items = when (expression.select) {
            Select.AllProjectedAttributes -> getBatch(expression.table, response.items ?: emptyList())
            else -> response.items ?: emptyList()
        }
        DbResult(expression.table, items, null).datas<T>().asFlow()
    }
}