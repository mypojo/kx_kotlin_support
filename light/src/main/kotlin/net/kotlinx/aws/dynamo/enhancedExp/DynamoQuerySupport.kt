package net.kotlinx.aws.dynamo.enhancedExp

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.Select
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
    return doUntilTokenNull { _, last ->
        expression.exclusiveStartKey = last as Map<String, AttributeValue>?
        val resp = this.query(expression.toQueryRequest())
        check(resp.items == null)
        listOf(resp.count) to resp.lastEvaluatedKey //doUntilTokenNull 형식에 맞추기 위해서 예쁘지 않게 코딩함.
    }.flatten().sum()
}


/** 전체 조회 -> 단축 */
suspend fun <T : DbItem> DynamoDbClient.queryAll(block: () -> DbExpression): List<T> = queryAll(block())

/** 전체 조회 */
suspend fun <T : DbItem> DynamoDbClient.queryAll(expression: DbExpression): List<T> {
    expression.limit = DbExpression.EXPRESSION_LIMIT //전부 가져올때는 강제로 최대치를 가져옴
    val itemMaps = doUntilTokenNull { _, last ->
        expression.exclusiveStartKey = last as Map<String, AttributeValue>?
        val result2 = query(expression)
        result2.maps to result2.lastEvaluatedKey
    }.flatten()
    return DbResult(expression.table, itemMaps, null).datas()
}
