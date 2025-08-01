package net.kotlinx.aws.dynamo.enhancedExp

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.paginators.scanPaginated
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import net.kotlinx.aws.dynamo.enhanced.DbItem
import net.kotlinx.collection.doUntilTokenNull


/**
 * 간단 스캔
 * 1M 단위 용량으로 리턴한다.
 * 용량 이내라면 limit 제한해서 리턴
 *  */
suspend fun DynamoDbClient.scan(exp: DbExpression): DbResult {
    val resp = this.scan(exp.toScanRequest())
    return DbResult(exp.table, resp.items!!, resp.lastEvaluatedKey)
}

/**
 * 전체 조회
 * 사용시 메모리 / 비용 주의!!
 *  */
@Deprecated("사용안함")
suspend fun <T : DbItem> DynamoDbClient.scanAll2(expression: DbExpression): List<T> {
    expression.limit = DbExpression.EXPRESSION_LIMIT //전부 가져올때는 강제로 최대치를 가져옴
    val itemMaps = doUntilTokenNull { _, last ->
        expression.exclusiveStartKey = last as Map<String, AttributeValue>?
        val result2 = scan(expression)
        result2.maps to result2.lastEvaluatedKey
    }.flatten()
    return DbResult(expression.table, itemMaps, null).datas()
}

/** 전체 조회 -> 단축 */
fun <T : DbItem> DynamoDbClient.scanAll(block: () -> DbExpression): Flow<T> = scanAll(block())

/**
 * 전체 조회
 * AWS SDK의 페이징 함수를 이용해 Flow를 반환
 * 메모리 효율성 향상
 */
fun <T : DbItem> DynamoDbClient.scanAll(expression: DbExpression): Flow<T> {
    expression.limit = DbExpression.EXPRESSION_LIMIT // 전부 가져올때는 강제로 최대치를 가져옴
    return this.scanPaginated(expression.toScanRequest()).flatMapConcat { response ->
        DbResult(expression.table, response.items ?: emptyList(), null).datas<T>().asFlow()
    }
}

