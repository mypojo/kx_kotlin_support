package net.kotlinx.aws.dynamo.query

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.batchGetItem
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.KeysAndAttributes
import aws.sdk.kotlin.services.dynamodb.model.Select
import net.kotlinx.aws.dynamo.DynamoBasic
import net.kotlinx.collection.doUntilTokenNull

//==================================================== 단일 쿼리 ======================================================

/** 일반 쿼리 */
suspend fun DynamoDbClient.query(exp: DynamoExpression): DynamoResp {
    val resp = this.query(exp.toQueryRequest())
    val firstQuery = resp.items!!
    if (firstQuery.isEmpty()) return DynamoResp(emptyList(), resp.lastEvaluatedKey)

    //부분 데이터만 가지고있을경우(보통 인덱스) 다시 조회 해준다. 이때 정렬이 풀림으로 재정렬 해줘야함
    val itemMaps = when (exp.select) {
        Select.AllProjectedAttributes -> batchGetItem(exp.tableName, firstQuery)
        else -> firstQuery
    }
    return DynamoResp(itemMaps, resp.lastEvaluatedKey)
}

/** 일반쿼리 단축 */
suspend fun DynamoDbClient.query(block: () -> DynamoExpression): DynamoResp = query(block())

/**
 * 일반쿼리 단축
 * 이거 괜찮은듯?  이거 베이스로 가자.
 *  */
suspend fun DynamoDbClient.queryAll(expression: DynamoExpression): List<Map<String, AttributeValue>> {
    return doUntilTokenNull { _, last ->
        expression.exclusiveStartKey = last as Map<String, AttributeValue>?
        val result2 = query(expression)
        result2.datas to result2.lastEvaluatedKey
    }.flatten()
}


/**
 * batchGetItem 의 로우 API 버전
 * 재정렬 작업을 추가해준다.
 *  */
suspend fun DynamoDbClient.batchGetItem(tableName: String, params: List<Map<String, AttributeValue>>): List<Map<String, AttributeValue>> {
    val orders = params.map { it[DynamoBasic.SK]!! } //정렬정보 저장
    val results = this.batchGetItem {
        this.requestItems = mapOf(
            tableName to KeysAndAttributes {
                this.keys = params.map {
                    mapOf(
                        DynamoBasic.PK to it[DynamoBasic.PK]!!,
                        DynamoBasic.SK to it[DynamoBasic.SK]!!,
                    )
                }
            }
        )
    }.responses!!.values.flatten()

    val groupBySk = results.associateBy { it[DynamoBasic.SK]!! }
    return orders.map { sk -> groupBySk[sk]!! }
}
