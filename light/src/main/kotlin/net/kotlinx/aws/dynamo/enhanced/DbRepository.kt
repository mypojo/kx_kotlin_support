package net.kotlinx.aws.dynamo.enhanced

import aws.sdk.kotlin.services.dynamodb.model.DeleteItemResponse
import kotlinx.coroutines.flow.Flow
import net.kotlinx.aws.LazyAwsClientProperty
import net.kotlinx.aws.dynamo.dynamo
import net.kotlinx.aws.dynamo.enhancedExp.*

/**
 * DDB 간단접근용 헬퍼
 * 실제 필요한 나머지 쿼리는 개별 코딩할것
 */
abstract class DbRepository<T : DbItem>() {

    var aws by LazyAwsClientProperty()

    protected abstract val dbTable: DbTable

    //==================================================== 기본 오버라이드 ======================================================

    suspend fun putItem(item: T): Unit = aws.dynamo.put(item)

    suspend fun updateItem(item: T, updateKeys: Collection<String>) = aws.dynamo.update(item, updateKeys)

    suspend fun getItem(item: T): T? = aws.dynamo.get(item)

    suspend fun getItem(pk: String, sk: String): T? = aws.dynamo.get(dbTable, pk, sk)

    suspend fun deleteItem(item: T): DeleteItemResponse = aws.dynamo.delete(item)

    suspend fun deleteItem(pk: String, sk: String) = aws.dynamo.delete(dbTable, pk, sk)

    //==================================================== 스레드 세이프 map 업데이트 ======================================================



    //==================================================== 벌크처리 ======================================================

    suspend fun getItemBatch(items: List<T>): List<T> = aws.dynamo.getBatch(items)

    //==================================================== 기본 쿼리 ======================================================

    /** 일반 조회 */
    suspend fun findPkSkEq(pk: String, sk: String? = null, block: DbExpression.() -> Unit = {}): DbResult = aws.dynamo.query { findPkSkEqInner(pk, sk, block) }

    /** 전체 조회 */
    fun findPkSkEqAll(pk: String, sk: String? = null, block: DbExpression.() -> Unit = {}): Flow<T> =
        aws.dynamo.queryAll { findPkSkEqInner(pk, sk, block) }

    /** 내부 템플릿 */
    private fun findPkSkEqInner(pk: String, sk: String? = null, block: DbExpression.() -> Unit): DbExpressionSet.PkSkEq = DbExpressionSet.PkSkEq {
        table = dbTable
        this.pk = pk
        this.sk = sk
        block(this)
    }

    //==================================================== 기본 스캔  ======================================================

    /** 페이징 X */
    suspend fun scan(): List<T> = aws.dynamo.scan(DbExpressionSet.None { this.table = dbTable }).datas()

    /** 사용시 주의!! */
    fun scanAll(): Flow<T> = aws.dynamo.scanAll(DbExpressionSet.None { table = dbTable })

}