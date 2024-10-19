package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.dynamo.query.DynamoExpression
import net.kotlinx.aws.dynamo.query.DynamoResult
import net.kotlinx.aws.dynamo.query.batchGetItem

/**
 * DDB 네이밍 맞춤용 리파지토리
 */
interface DynamoRepository<T : DynamoData> {

    val aws: AwsClient

    /** 네이블 네임 & 컨버터용 */
    val emptyData: T

    suspend fun putItem(item: T) {
        aws.dynamo.putItem(item)
    }

    suspend fun updateItem(item: T, updateKeys: List<String>) {
        aws.dynamo.updateItem(item, updateKeys)
    }

    suspend fun getItem(item: T): T? = aws.dynamo.getItem(item)

    suspend fun batchGetItem(items: List<T>): List<T> = aws.dynamo.batchGetItem(items)

    suspend fun deleteItem(item: T) {
        aws.dynamo.deleteItem(item)
    }

    suspend fun scan(exp: DynamoExpression? = null, last: Map<String, AttributeValue>? = null): DynamoResult<T> = aws.dynamo.scan(emptyData, exp, last)

    suspend fun scanAll(exp: DynamoExpression? = null): List<T> = aws.dynamo.scanAll(emptyData, exp)

}