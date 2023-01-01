package net.kotlinx.aws1.dynamo

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.ReturnValue
import aws.sdk.kotlin.services.dynamodb.updateItem

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