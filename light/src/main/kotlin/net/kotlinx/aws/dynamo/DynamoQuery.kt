package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.QueryRequest
import aws.sdk.kotlin.services.dynamodb.model.Select
import mu.KotlinLogging
import net.kotlinx.core.Kdsl

/**
 * 각 항목들은 상속해서 사용
 *
 * 상세 문법은 아래 참고
 * https://docs.aws.amazon.com/ko_kr/amazondynamodb/latest/developerguide/Query.KeyConditionExpressions.html
 * */
class DynamoQuery {

    @Kdsl
    constructor(block: DynamoQuery.() -> Unit = {}) {
        apply(block)
    }

    lateinit var param: Map<String, AttributeValue>
    lateinit var query: String

    /**
     * 주어진 데이터로 쿼리 파라메터를 어떻게 만들것인지 정의
     * 간단히 정의 가능한곳에만 사용.
     *  */
    var createParamAndQuery: ((data: DynamoData) -> Map<String, AttributeValue>)? = null

    var limit: Int = 100

    /** 특별한일 없으면 사용금지 */
    var consistentRead: Boolean = false

    /** Select.AllAttributes / AllProjectedAttributes 등등. null 하면 알아서 잘 되는듯. */
    var select: Select? = null

    /** 인덱스 이름 */
    var indexName: String? = null

    /**
     * 디폴트로 역순 조회인 false
     * 최신데이터를 원하는경우가 대부분임
     *  */
    var scanIndexForward: Boolean? = false

    /** 페이징에서 받은 last key */
    var exclusiveStartKey: Map<String, AttributeValue>? = null

    /** 수정해서 사용 */
    fun toQueryRequest(data: DynamoData): QueryRequest {

        log.trace { "쿼리 파라메터 생성 로직이 있다면 적용" }
        createParamAndQuery?.let {
            val returnParam = it(data)
            this.param = returnParam
            this.query = returnParam.keys.joinToString(" AND ") { "${it.replaceFirst(":", "")} = $it" }
        }
        check(this::param.isInitialized)
        check(this::query.isInitialized)
        return QueryRequest {
            this.expressionAttributeValues = param
            this.keyConditionExpression = query
            this.tableName = data.tableName
            this.consistentRead = this@DynamoQuery.consistentRead
            this.limit = this@DynamoQuery.limit
            this.select = this@DynamoQuery.select
            this.indexName = this@DynamoQuery.indexName
            this.scanIndexForward = this@DynamoQuery.scanIndexForward
            this.exclusiveStartKey = this@DynamoQuery.exclusiveStartKey
        }
    }

    companion object {

        private val log = KotlinLogging.logger {}

    }


}