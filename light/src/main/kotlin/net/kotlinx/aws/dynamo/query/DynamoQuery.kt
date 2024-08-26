package net.kotlinx.aws.dynamo.query

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.QueryRequest
import aws.sdk.kotlin.services.dynamodb.model.Select
import mu.KotlinLogging
import net.kotlinx.core.Kdsl

/**
 * 원래 QueryRequest 의 기본세팅 정의 버전
 *
 * 상세 문법은 아래 참고
 * https://docs.aws.amazon.com/ko_kr/amazondynamodb/latest/developerguide/Query.KeyConditionExpressions.html
 * */
class DynamoQuery {

    @Kdsl
    constructor(block: DynamoQuery.() -> Unit = {}) {
        apply(block)
    }

    /** 쿼리 표현식 */
    lateinit var expression: DynamoExpression

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
    fun toQueryRequest(tableName: String): QueryRequest {

        log.trace { "쿼리 파라메터 생성 로직이 있다면 적용" }
        return QueryRequest {
            this.expressionAttributeValues = expression.expressionAttributeValues()
            this.keyConditionExpression = expression.expression()
            this.tableName = tableName
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