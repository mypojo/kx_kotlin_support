package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.DynamoDbException
import aws.sdk.kotlin.services.dynamodb.putItem
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

/**
 * 다이나모DB를 사용한 글로벌 유니크 키 채번기
 * 미리 테이블이 만들어져있어야 한다.
 * @see net.kotlinx.core.id.IdGenerator
 */

class DynamoIdSource(
    /**  client */
    private val dynamoDbClient: DynamoDbClient,
    /** 파티션키 1개만 사용가능. 보통 프로젝트 명. 편의상 문자열로 고정한다.  */
    private val pkValue: String,
    /** DDB 테이블명 */
    private val seqTableName: String = "guid",
    /** DDB 테이블의 PK 이름  */
    private val pkName: String = "pk",
    /** 채번할 키값 컬럼명 */
    private val idSeqColumnName: String = "id_seq",
) : () -> Long {

    private val log = KotlinLogging.logger {}

    /** 최초 1회 생성  */
    suspend fun create() {
        dynamoDbClient.putItem {
            this.tableName = seqTableName
            this.item = mapOf(
                pkName to AttributeValue.S(pkValue),
                idSeqColumnName to AttributeValue.N("0")
            )
        }
    }

    /** 최초 수행시 없다면 자동 생성 해준다.  */
    override fun invoke(): Long = runBlocking {
        return@runBlocking try {
            increaseAndGet()
        } catch (e: DynamoDbException) {
            create()
            log.warn("테이블 [{}] : 신규 seq 컬럼[{}] => 생성됨", seqTableName, pkName)
            increaseAndGet()
        }
    }

    @Throws(DynamoDbException::class)
    private suspend fun increaseAndGet(): Long = dynamoDbClient.increaseAndGet(seqTableName, pkName, pkValue, idSeqColumnName)

}
