package net.kotlinx.aws.ddb

import aws.sdk.kotlin.services.dynamodb.*
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.ReturnValue
import net.kotlinx.koin.Koins.koin


//==================================================== 입력 ======================================================

suspend inline fun <reified T : DdbData> DynamoDbClient.putData(item: T) = putData(item, koin<DdbTableConfig<T>>())

suspend fun <T : DdbData> DynamoDbClient.putData(item: T, config: DdbTableConfig<T>) {
    this.putItem {
        this.tableName = config.tableName
        this.item = config.converter.toAttribute(item)
    }
}

//==================================================== 수정 ======================================================

suspend inline fun <reified T : DdbData> DynamoDbClient.updateData(item: T, updateKeys: Set<String>) = updateData(item, updateKeys, koin<DdbTableConfig<T>>())

suspend fun <T : DdbData> DynamoDbClient.updateData(item: T, updateKeys: Set<String>, config: DdbTableConfig<T>) {
    this.updateItem {
        this.tableName = config.tableName
        this.key = item.toKeyMap()
        this.updateExpression = "SET ${updateKeys.joinToString(",") { "$it = :${it}" }}"
        this.expressionAttributeValues = config.converter.toAttribute(item).filterKeys { it in updateKeys }.mapKeys { ":${it.key}" }
        this.returnValues = ReturnValue.AllNew
    }
}

//==================================================== 조회 ======================================================

suspend inline fun <reified T : DdbData> DynamoDbClient.getData(item: T) = getData(item, koin<DdbTableConfig<T>>())

suspend fun <T : DdbData> DynamoDbClient.getData(item: T, config: DdbTableConfig<T>): T? {
    val map: Map<String, AttributeValue> = this.getItem {
        this.tableName = config.tableName
        this.consistentRead = false
        this.key = item.toKeyMap()
    }.item ?: return null
    return config.converter.fromAttributeMap(map)
}


//==================================================== 삭제 ======================================================

suspend inline fun <reified T : DdbData> DynamoDbClient.deleteData(item: T, returnValue: ReturnValue = ReturnValue.None) = deleteData(item, returnValue, koin<DdbTableConfig<T>>())

suspend fun <T : DdbData> DynamoDbClient.deleteData(item: T, returnValue: ReturnValue = ReturnValue.None, config: DdbTableConfig<T>) {
    this.deleteItem {
        this.tableName = config.tableName
        this.key = item.toKeyMap()
        this.returnValues = returnValue
    }
}

