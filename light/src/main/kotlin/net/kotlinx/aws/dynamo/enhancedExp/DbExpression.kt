package net.kotlinx.aws.dynamo.enhancedExp

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.QueryRequest
import aws.sdk.kotlin.services.dynamodb.model.Select
import net.kotlinx.aws.dynamo.enhanced.DbItem
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.aws.dynamo.enhanced.DbTable.Companion.PK_NAME
import net.kotlinx.aws.dynamo.enhanced.DbTable.Companion.SK_NAME


/**
 * 쿼리 표현식 샘플
 * https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.ConditionExpressions.html
 *
 * 실제 쿼리보다는 이게 더 중심이 됨으로 이 객체 중심으로 쿼리를 작성한다.
 * abstract 로 꾸며보니 코드 보기 나쁘지 않아서 이렇게 함
 *
 * 범용으로 써야해서 제너릭 하게 만들지 않음
 * */
abstract class DbExpression {

    //==================================================== 간편설정 ======================================================

    /**
     * 인덱스 없이 expression 생성.
     * 이 방법은 sk 생성 등에서 null 체크가 애매해서 추천하지 않음
     *  */
    fun init(item: DbItem) {
        table = item.table()
        pkName = table.pkName
        skName = table.skName
        pk = item.pk
        sk = item.sk
    }

    /** 인덱스로 expression 생성 */
    fun init(table: DbTable, indexName: String) {
        this.table = table
        this.indexName = indexName
    }

    fun pk(nameValue: Pair<String, String>) {
        pkName = nameValue.first
        pk = nameValue.second
    }

    fun sk(nameValue: Pair<String, String>) {
        skName = nameValue.first
        sk = nameValue.second
    }

    //==================================================== 입력값 ======================================================

    /** 테이블. 최종 API 호출시 테이블명을 알아오기 위해 사용됨 */
    lateinit var table: DbTable

    /** PK */
    lateinit var pk: String

    /** PK 컬럼 이름. 인덱스 사용시 기본이름이 아닐 수 있음!!  */
    var pkName: String = PK_NAME

    /** SK */
    var sk: String? = null

    /** SK 컬럼 이름. 인덱스 사용시 기본이름이 아닐 수 있음!! */
    var skName: String = SK_NAME

    //==================================================== 설정값 ======================================================

    /**
     * 페이징 리미트. DDB 디폴트?료는 용량(1m?)으로 짤림
     * 보통 job 같은 객체는 1000개  이상도 나옴
     * all로 가져오려면 최대치로 가져와야함
     *  */
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


    /** 실제 쿼리 요청으로 변경 */
    fun toQueryRequest(): QueryRequest {
        return QueryRequest {
            //==================================================== expression ======================================================
            this.keyConditionExpression = keyConditionExpression()
            this.expressionAttributeValues = expressionAttributeValues()
            this.filterExpression = filterExpression()

            //==================================================== key ======================================================
            this.tableName = this@DbExpression.table.tableName
            this.exclusiveStartKey = this@DbExpression.exclusiveStartKey

            //==================================================== 기타(조정할일 크게 없음)  ======================================================
            this.consistentRead = this@DbExpression.consistentRead
            this.limit = this@DbExpression.limit
            this.select = this@DbExpression.select
            this.indexName = this@DbExpression.indexName
            this.scanIndexForward = this@DbExpression.scanIndexForward
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
    abstract fun expressionAttributeValues(): Map<String, AttributeValue>?

    companion object {

        /**
         * 쿼리나 스캔의 최대 페이징 수
         * 어차피 용량단위로 잘라옴으로 크게 의미없고
         * all 로 가져올때는 큰 수를 넣어야 최적화.
         *  */
        const val EXPRESSION_LIMIT = 10000


        /** 벌크 GET 요청시 최대 허용수 */
        const val GET_LIMIT = 100

    }

}