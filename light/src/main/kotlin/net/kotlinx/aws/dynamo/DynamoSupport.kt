package net.kotlinx.aws.dynamo


import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.executeStatement
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist
import net.kotlinx.collection.doUntilTokenNull


val AwsClient.dynamo: DynamoDbClient
    get() = getOrCreateClient {
        DynamoDbClient {
            awsConfig.build(this)
            interceptors += awsConfig.ddbInterceptors
        }.regist(awsConfig)
    }




/** 자주 사용되는 값 */
typealias DynamoMap = Map<String, AttributeValue>

/** PartiQL로 데이터를 Flow로 가져오기 */
fun DynamoDbClient.executeStatementAll(query: String): Flow<Map<String, AttributeValue>> = flow {
    doUntilTokenNull { _, last ->
        val resp = this@executeStatementAll.executeStatement {
            this.statement = query
            this.nextToken = last as String?
        }
        val items = resp.items ?: emptyList()
        items.forEach { emit(it) }
        items to resp.nextToken
    }
}

