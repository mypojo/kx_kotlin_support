package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.executeStatement
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist
import net.kotlinx.collection.doUntilTokenNull

val AwsClient.dynamo: DynamoDbClient
    get() = getOrCreateClient { DynamoDbClient { awsConfig.build(this) }.regist(awsConfig) }


/** 자주 사용되는 값 */
typealias DynamoMap = Map<String, AttributeValue>

/**
 * PartiQL 로 데이터 가져오기
 * PartiQL로는 단건 삭제가 안됨 -> 이걸로 읽은 후 삭제 해주면 됨
 * 프로그래밍적으로 하는게 좋지만, 이게 더 빠르고 직관적일때도 있다.
 * */
suspend fun DynamoDbClient.executeStatementAll(query: String): List<Map<String, AttributeValue>> {
    return doUntilTokenNull { _, last ->
        val resp = this.executeStatement {
            this.statement = query
            this.nextToken = last as String?
        }
        val items = resp.items ?: emptyList()
        items to resp.nextToken
    }.flatten()
}