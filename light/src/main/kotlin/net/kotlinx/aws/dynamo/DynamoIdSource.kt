package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.DynamoDbException
import aws.sdk.kotlin.services.dynamodb.putItem
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.kotlinx.aws.dynamo.enhanced.DbTable

/**
 * 다이나모DB를 사용한 글로벌 유니크 키 채번기
 * 미리 테이블이 만들어져있어야 한다.
 * @see net.kotlinx.id.IdGenerator
 */

class DynamoIdSource(
    /**  client */
    private val dynamoDbClient: DynamoDbClient,
    /** 파티션키 1개만 사용가능. 보통 프로젝트 명. 편의상 문자열로 고정한다.  */
    private val pkValue: String,
    /** DDB 테이블명 */
    private val seqTableName: String = "guid",
    /** 채번할 키값 컬럼명 */
    private val idSeqColumnName: String = "id_seq",
    /** 정렬키 값 (시퀀스용 기본값) -> 사실 필요는 없는데 sk 자리를 비우기 싫어서 강제로 채움 */
    private val skValue: String = "seq",
) : () -> Long {

    private val log = KotlinLogging.logger {}

    /** 최초 1회 생성  */
    suspend fun create() {
        dynamoDbClient.putItem {
            this.tableName = seqTableName
            this.item = mapOf(
                DbTable.PK_NAME to AttributeValue.S(pkValue),
                DbTable.SK_NAME to AttributeValue.S(skValue),
                idSeqColumnName to AttributeValue.N("0")
            )
        }
    }

    /** 최초 수행시 없다면 자동 생성 해준다.  */
    override fun invoke(): Long = runBlocking {
        return@runBlocking try {
            increaseAndGet()
        } catch (_: DynamoDbException) {
            create()
            log.warn { "테이블 [${seqTableName}] : 신규 seq 아이템 생성됨 (pk=${pkValue}, sk=${skValue})" }
            increaseAndGet()
        }
    }

    @Throws(DynamoDbException::class)
    private suspend fun increaseAndGet(): Long = dynamoDbClient.increaseAndGet(seqTableName, pkValue, skValue, idSeqColumnName)

}
