package net.kotlinx.domain.ddb

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.AwsClient1
import net.kotlinx.aws.dynamo.deleteItem
import net.kotlinx.aws.dynamo.getItem
import net.kotlinx.aws.dynamo.putItem
import net.kotlinx.aws.dynamo.query.DynamoExpression
import net.kotlinx.aws.dynamo.query.DynamoResult
import net.kotlinx.aws.dynamo.query.query
import net.kotlinx.aws.dynamo.query.queryAll
import net.kotlinx.koin.Koins.koinLazy

typealias aa<T> = suspend (item: T, last: Map<String, AttributeValue>?) -> DynamoResult<T>

/** 기본 저장소 */
class DdbBasicRepository<T>(
    private val profile: String? = null,
    private val converter: DdbBasicConverter<DdbBasic, T>,
) {

    private val aws by koinLazy<AwsClient1>(profile)

    //==================================================== 기본 메소드 ======================================================
    suspend fun getItem(item: T): T? {
        val query = converter.convertFrom(item)
        val ddb = aws.dynamo.getItem(query) ?: return null
        return converter.convertTo(ddb)
    }

    suspend fun putItem(item: T) {
        val ddb = converter.convertFrom(item)
        aws.dynamo.putItem(ddb)
    }

    suspend fun deleteItem(item: T) {
        val query = converter.convertFrom(item)
        aws.dynamo.deleteItem(query)
    }

    //==================================================== 기본 쿼리 ======================================================

    /** 인덱스 없이 SK 프리픽스 기반으로 조회 */
    suspend fun findBySkPrefix(item: T, last: Map<String, AttributeValue>? = null, block: DynamoExpression.() -> Unit = {}): DynamoResult<T> {
        val (basic, express) = converter.findBySkPrefix(item, last)
        block(express)
        val result2 = aws.dynamo.query(express)
        val datas = result2.datas.map { basic.fromAttributeMap<DdbBasic>(it) }.map { converter.convertTo(it) }
        return DynamoResult(datas, result2.lastEvaluatedKey)
    }

    /** 인덱스 없이 SK 프리픽스 기반으로 조회 */
    suspend fun findAllBySkPrefix(data: T, block: DynamoExpression.() -> Unit = {}): List<T> {
        val (basic, express) = converter.findBySkPrefix(data, null)
        block(express)
        val results = aws.dynamo.queryAll(express)
        return results.map { basic.fromAttributeMap<DdbBasic>(it) }.map { converter.convertTo(it) }
    }

    //==================================================== 인덱스 쿼리 ======================================================

    /** 인덱스 없이 SK 프리픽스 기반으로 조회 */
    suspend fun findBySkPrefix(index: DdbBasicGsi,item: T, last: Map<String, AttributeValue>? = null, block: DynamoExpression.() -> Unit = {}): DynamoResult<T> {
        val (basic, express) = converter.findBySkPrefix(index,item, last)
        block(express)
        val result2 = aws.dynamo.query(express)
        val datas = result2.datas.map { basic.fromAttributeMap<DdbBasic>(it) }.map { converter.convertTo(it) }
        return DynamoResult(datas, result2.lastEvaluatedKey)
    }

    /** 인덱스 없이 SK 프리픽스 기반으로 조회 */
    suspend fun findAllBySkPrefix(index: DdbBasicGsi,data: T, block: DynamoExpression.() -> Unit = {}): List<T> {
        val (basic, express) = converter.findBySkPrefix(index,data, null)
        block(express)
        val results = aws.dynamo.queryAll(express)
        return results.map { basic.fromAttributeMap<DdbBasic>(it) }.map { converter.convertTo(it) }
    }

}