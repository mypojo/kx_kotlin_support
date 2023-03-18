package net.kotlinx.aws1.dynamo

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.QueryRequest
import aws.sdk.kotlin.services.dynamodb.model.Select
import net.kotlinx.aws1.dynamo.DynamoQuery.DynamoQueryKeyEqualTo
import net.kotlinx.aws1.dynamo.DynamoQuery.DynamoQuerySortBeginsWith


/** 권장하는 키값 스타일 마킹 인터페이스 */
interface DynamoDbBasic {

    val pk: String
    val sk: String

    companion object {
        const val pk = "pk"
        const val sk = "sk"
    }
}

/** 제너릭 때문에 이 클래스를 끝까지 유지 해야함 */
interface DynamoData : DynamoDbBasic {

    val tableName: String

    fun toKeyMap(): Map<String, AttributeValue> {
        return mapOf(
            DynamoDbBasic.pk to AttributeValue.S(this.pk),
            DynamoDbBasic.sk to AttributeValue.S(this.sk),
        )
    }

    //==================================================== 개별 구현 ======================================================

    /** 이건 객체마다 해줘야함 */
    fun toAttribute(): Map<String, AttributeValue>

    /** 이건 객체마다 해줘야함 */
    fun <T : DynamoData> fromAttributeMap(map: Map<String, AttributeValue>): T
}

/**
 * 각 항목들은 상속해서 사용
 * 상세 쿼리는 QueryConditional 참고
 * */
abstract class DynamoQuery {

    abstract val query: String
    abstract fun param(data: DynamoData): Map<String, AttributeValue>

    open var limit: Int = 100
    open var consistentRead = false
    open var select = Select.AllAttributes
    open var indexName: String? = null

    /** 역순 조회하려면 false */
    open var scanIndexForward: Boolean? = true

    /** 수정해서 사용 */
    open fun toQueryRequest(data: DynamoData): QueryRequest = QueryRequest {
        this.expressionAttributeValues = param(data)
        this.keyConditionExpression = query
        this.tableName = data.tableName
        this.consistentRead = this@DynamoQuery.consistentRead
        this.limit = this@DynamoQuery.limit
        this.select = Select.AllAttributes
        this.indexName = this@DynamoQuery.indexName
        this.scanIndexForward = this@DynamoQuery.scanIndexForward
    }

    //==================================================== 쿼리 샘플 ======================================================

    open class DynamoQueryKeyEqualTo : DynamoQuery() {
        override val query: String = "${DynamoDbBasic.pk} = :${DynamoDbBasic.pk}"
        override fun param(data: DynamoData): Map<String, AttributeValue> = mapOf(":${DynamoDbBasic.pk}" to AttributeValue.S(data.pk))
    }

    open class DynamoQuerySortBeginsWith : DynamoQuery() {
        override val query: String = "${DynamoDbBasic.pk} = :${DynamoDbBasic.pk} AND begins_with(${DynamoDbBasic.sk}, :${DynamoDbBasic.sk})"
        override fun param(data: DynamoData): Map<String, AttributeValue> = mapOf(":${DynamoDbBasic.pk}" to AttributeValue.S(data.pk), ":${DynamoDbBasic.sk}" to AttributeValue.S(data.sk))
    }

}

/** 쿼리 샘플 모음 */
object DynamoQuerySet {
    /** PK 일치*/
    object keyEqualTo : DynamoQueryKeyEqualTo()

    /** PK 일치 &  SK 접두어 조회  */
    object sortBeginsWith : DynamoQuerySortBeginsWith()
}
