package net.kotlinx.aws.dynamo.enhanced

import net.kotlinx.aws.dynamo.dynamo
import net.kotlinx.aws.dynamo.enhancedExp.DbExpressionSet
import net.kotlinx.aws.dynamo.enhancedExp.scan
import net.kotlinx.aws.dynamo.enhancedExp.scanAll
import net.kotlinx.aws.lazyAwsClient
import net.kotlinx.domain.job.Job

/**
 * DDB 간단접근용 헬퍼
 * 실제 필요한 나머지 쿼리는 개별 코딩할것
 */
abstract class DbRepository<T : DbItem>() {

    var aws by lazyAwsClient()

    protected abstract val dbTable: DbTable

    //==================================================== 기본 오버라이드 ======================================================

    suspend fun putItem(item: T): Unit = aws.dynamo.put(item)

    suspend fun updateItem(item: T, updateKeys: Collection<String>) = aws.dynamo.update(item, updateKeys)

    suspend fun getItem(item: T): T? = aws.dynamo.get(item)

    suspend fun getItem(pk: String, sk: String): T? = aws.dynamo.get(dbTable, pk, sk)

    suspend fun deleteItem(item: T): Unit = aws.dynamo.delete(item)

    //==================================================== 벌크처리 ======================================================

    suspend fun getItemBatch(items: List<T>): List<T> = aws.dynamo.getBatch(items)

    //==================================================== 기본 스캔  ======================================================

    /** 페이징 X */
    suspend fun scan(): List<Job> = aws.dynamo.scan(DbExpressionSet.None { this.table = dbTable }).datas()

    /** 사용시 주의!! */
    suspend fun scanAll(): List<Job> = aws.dynamo.scanAll(DbExpressionSet.None { table = dbTable })

}