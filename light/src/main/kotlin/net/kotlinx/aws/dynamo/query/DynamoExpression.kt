package net.kotlinx.aws.dynamo.query

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.QueryRequest
import aws.sdk.kotlin.services.dynamodb.model.Select
import net.kotlinx.aws.dynamo.DynamoBasic


/**
 * 쿼리 표현식 샘플
 * https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.ConditionExpressions.html
 *
 * 실제 쿼리보다는 이게 더 중심이 됨으로 이 객체 중심으로 쿼리를 작성한다.
 * */
abstract class DynamoExpression {

    //==================================================== 키값들 ======================================================

    var pkName: String = DynamoBasic.PK
    var skName: String = DynamoBasic.SK

    /** PK 값 */
    lateinit var pk: String

    /** SK 값 */
    var sk: String? = null

    //==================================================== 이하 쿼리 생성용 ======================================================

    /** 테이블명 */
    lateinit var tableName: String

    /** 페이징 리미트. DDB 디폴트?료는 용량(1m?)으로 짤림 */
    var limit: Int = 100

    /** 특별한일 없으면 사용금지 */
    var consistentRead: Boolean = false

    /**
     * Select.AllProjectedAttributes : 인덱스에서 키값만 조회 후 전체 데이터 재조회
     */
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

    //==================================================== 메소드 ======================================================

    fun toQueryRequest(): QueryRequest {
        return QueryRequest {
            //==================================================== expression ======================================================
            this.keyConditionExpression = keyConditionExpression()
            this.expressionAttributeValues = expressionAttributeValues()
            this.filterExpression = filterExpression()

            //==================================================== key ======================================================
            this.tableName = this@DynamoExpression.tableName
            this.exclusiveStartKey = this@DynamoExpression.exclusiveStartKey

            //==================================================== 기타(조정할일 크게 없음)  ======================================================
            this.consistentRead = this@DynamoExpression.consistentRead
            this.limit = this@DynamoExpression.limit
            this.select = this@DynamoExpression.select
            this.indexName = this@DynamoExpression.indexName
            this.scanIndexForward = this@DynamoExpression.scanIndexForward
        }
    }

    /**
     * 메인 키나 GSI 대상의 쿼리만 가능함
     * IN 같은 쿼리 불가능
     * */
    open fun keyConditionExpression(): String? = null

    /**
     * 풀스캔할때 사용 or DDB 서버에서 쿼리 이후 네트워크로 가져오기 싫을때 사용
     * 이미 스캔한거 필터링하는 용도임.
     * IN 같은거도 사용가능
     * */
    open fun filterExpression(): String? = null

    /**
     * 표현식의 실제 값
     * */
    abstract fun expressionAttributeValues(): Map<String, AttributeValue>

}