package net.kotlinx.aws.dynamo.multiIndex

import net.kotlinx.aws.dynamo.DynamoMap
import net.kotlinx.aws.dynamo.enhancedExp.DbExpression


/** 제너릭이 적용된 멀티 인덱스용 기본 저장소 */
class DbMultiindexItemGenericRepository<T>(
    private val repository: DbMultiIndexItemRepository,
    private val eachConverter: DbMultiIndexEachConverter<DbMultiIndexItem, T>,
) {

    //==================================================== 기본 메소드 ======================================================
    suspend fun getItem(item: T): T? {
        val param = eachConverter.convertFrom(item)
        val ddb = repository.getItem(param) ?: return null
        return eachConverter.convertTo(ddb)
    }

    suspend fun putItem(item: T) {
        val ddb = eachConverter.convertFrom(item)
        repository.putItem(ddb)
    }

    suspend fun deleteItem(item: T) {
        val query = eachConverter.convertFrom(item)
        repository.deleteItem(query)
    }

    //==================================================== 기본 쿼리 ======================================================

    /** 페이징 조회 */
    suspend fun findBySkPrefix(item: T, index: DbMultiIndex? = null, block: DbExpression.() -> Unit = {}): Pair<List<T>, DynamoMap?> {
        val param = eachConverter.convertFrom(item)
        val dbResult = repository.findBySkPrefix(param, index, block)
        val datas = dbResult.datas<DbMultiIndexItem>().map { eachConverter.convertTo(it) }
        return datas to dbResult.lastEvaluatedKey
    }

    /** 전체 조회 */
    suspend fun findAllBySkPrefix(item: T, index: DbMultiIndex? = null, block: DbExpression.() -> Unit = {}): List<T> {
        val param = eachConverter.convertFrom(item)
        val dbResult = repository.findAllBySkPrefix(param, index, block)
        return dbResult.map { eachConverter.convertTo(it) }
    }

    /** 카운트만 조회 */
    suspend fun findCntBySkPrefix(item: T, index: DbMultiIndex? = null, block: DbExpression.() -> Unit = {}): Int {
        val param = eachConverter.convertFrom(item)
        return repository.findCntBySkPrefix(param, index, block)
    }

}