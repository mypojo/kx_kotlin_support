package net.kotlinx.aws.ddb.exp

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.scan
import net.kotlinx.aws.ddb.DbItem
import net.kotlinx.collection.doUntilTokenNull


/**
 * 간단 스캔
 * 1M 단위 용량으로 리턴한다.
 * 용량 이내라면 limit 제한해서 리턴
 *  */
suspend fun DynamoDbClient.scan(exp: DbExpression): DbResult {
    val resp = this.scan {
        this.consistentRead = false //읽기 일관성 사용안함
        this.tableName = exp.table.tableName
        this.exclusiveStartKey = exp.exclusiveStartKey
        this.limit = exp.limit
        this.filterExpression = exp.filterExpression()
        this.expressionAttributeValues = exp.expressionAttributeValues()
    }
    return DbResult(exp.table, resp.items!!, resp.lastEvaluatedKey)
}

/**
 * 전체 조회
 * 사용시 메모리 / 비용 주의!!
 *  */
suspend fun <T : DbItem> DynamoDbClient.scanAll(expression: DbExpression): List<T> {
    expression.limit = DbExpression.MAX_LIMIT //전부 가져올때는 강제로 최대치를 가져옴
    val itemMaps = doUntilTokenNull { _, last ->
        expression.exclusiveStartKey = last as Map<String, AttributeValue>?
        val result2 = scan(expression)
        result2.maps to result2.lastEvaluatedKey
    }.flatten()
    return DbResult(expression.table, itemMaps, null).datas()
}