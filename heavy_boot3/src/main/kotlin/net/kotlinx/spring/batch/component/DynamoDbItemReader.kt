package net.kotlinx.spring.batch.component

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.QueryResponse
import aws.sdk.kotlin.services.dynamodb.paginators.queryPaginated
import kotlinx.coroutines.flow.Flow
import org.springframework.batch.item.ItemReader

/**
 * AWS kotlin DDB 리더
 * Iterator 로 래핑하지 않는다. 스트림 상황에서는 hasNext() 를 구현할 수 없음
 * 필요할때 구현할것!!
 * 기본 SDK가 Flow로 리턴해서 짜증남!! 코드 복붙해서 토큰 이어가게
 * @see  DynamoDbClient.queryPaginated
 */
class DynamoDbItemReader(queryResult: Flow<QueryResponse>) : ItemReader<Map<String, AttributeValue>> {

    //private val paginated = sequenceOf(queryResult).iterator()

    @Synchronized
    override fun read(): Map<String, AttributeValue> {
        throw UnsupportedOperationException()
    }
}
