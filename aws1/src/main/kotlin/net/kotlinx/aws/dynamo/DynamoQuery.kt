package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.QueryRequest
import aws.sdk.kotlin.services.dynamodb.model.Select

/**
 * 각 항목들은 상속해서 사용
 * 상세 쿼리는 QueryConditional 참고
 * */
class DynamoQuery(block: DynamoQuery.() -> Unit) {

    lateinit var param: Map<String, AttributeValue>
    lateinit var query: String

    /**
     * 1:1
     *  */
    var queryParam: ((data: DynamoData) -> Map<String, AttributeValue>)? = null

    var limit: Int = 100

    /** 특별한일 없으면 사용금지 */
    var consistentRead: Boolean = false

    /** Select.AllAttributes / AllProjectedAttributes 등등. null 하면 알아서 잘 되는듯. */
    var select: Select? = null

    /** 인덱스 이름 */
    var indexName: String? = null

    /** 역순 조회하려면 false */
    var scanIndexForward: Boolean? = true

    init {
        block(this)
    }

    /** 수정해서 사용 */
    fun toQueryRequest(data: DynamoData): QueryRequest {
        queryParam?.let {
            val returnParam = it(data)
            this.param = returnParam
            this.query = returnParam.keys.joinToString(" AND ") { "${it.replaceFirst(":", "")} = $it" }
        }
        return QueryRequest {
            this.expressionAttributeValues = param
            this.keyConditionExpression = query
            this.tableName = data.tableName
            this.consistentRead = this@DynamoQuery.consistentRead
            this.limit = this@DynamoQuery.limit
            this.select = this@DynamoQuery.select
            this.indexName = this@DynamoQuery.indexName
            this.scanIndexForward = this@DynamoQuery.scanIndexForward
        }
    }

    companion object {

        val keyEqualTo = DynamoQuery {
            queryParam = { mapOf(":${DynamoDbBasic.pk}" to AttributeValue.S(it.pk)) }
        }
        val sortBeginsWith = DynamoQuery {
            queryParam = {
                mapOf(
                    ":${DynamoDbBasic.pk}" to AttributeValue.S(it.pk),
                    ":${DynamoDbBasic.sk}" to AttributeValue.S(it.sk),
                )
            }
        }
    }


}