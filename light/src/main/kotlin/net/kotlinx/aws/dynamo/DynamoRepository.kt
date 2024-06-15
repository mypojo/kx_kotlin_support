package net.kotlinx.aws.dynamo

import net.kotlinx.aws.AwsClient1

/**
 * DDB 네이밍 맞춤용 리파지토리
 */
interface DynamoRepository<T : DynamoData> {

    val aws: AwsClient1

    suspend fun putItem(item: T) {
        aws.dynamo.putItem(item)
    }

    suspend fun updateItem(item: T, updateKeys: List<String>) {
        aws.dynamo.updateItem(item, updateKeys)
    }

    suspend fun getItem(item: T): T? {
        return aws.dynamo.getItem(item)
    }

    suspend fun deleteItem(item: T) {
        aws.dynamo.deleteItem(item)
    }

}