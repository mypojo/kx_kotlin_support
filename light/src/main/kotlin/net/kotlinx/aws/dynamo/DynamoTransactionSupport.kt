package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.ReturnValue
import aws.sdk.kotlin.services.dynamodb.updateItem

//==================================================== 트랜잭션 ======================================================
//데이터 타입은 아래 문서 참고
//https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.NamingRulesDataTypes.html#HowItWorks.DataTypes
//update 참고
//https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.UpdateExpressions.html#Expressions.UpdateExpressions.ADD

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

/**
 * 해당 로우의 특정 map type(M – Map) 컬럼값에 synch하게  add(오버라이드) 후 리턴하는 샘플
 * 1. 기존 map에 데이터가 많아도, 추가만 수행한다.
 * 2. 해당 컬럼에 map이 없으면 작동하지 않는다. 빈 map 이라도 반드시 존재해야 한다
 * */
suspend fun DynamoDbClient.updateItemMap(tableName: String, pk: String, sk: String, columnName: String, append: Map<String, String>) {
    this.updateItem {
        this.tableName = tableName
        this.returnValues = ReturnValue.None //필요한 경우 없음.
        this.key = mapOf(
            DynamoBasic.PK to AttributeValue.S(pk),
            DynamoBasic.SK to AttributeValue.S(sk),
        )
        this.updateExpression = "set " + append.entries.joinToString(",") { "${columnName}.${it.key} = :${it.key}" }
        this.expressionAttributeValues = append.map { ":${it.key}" to AttributeValue.S(it.value) }.toMap()
    }
}