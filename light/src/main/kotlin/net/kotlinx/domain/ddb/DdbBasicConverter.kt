package net.kotlinx.domain.ddb

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.Select
import mu.KotlinLogging
import net.kotlinx.aws.dynamo.query.DynamoExpression
import net.kotlinx.aws.dynamo.query.DynamoExpressionSet
import net.kotlinx.core.DataConverter
import net.kotlinx.reflect.name


interface DdbBasicConverter<A : DdbBasic, B> : DataConverter<A, B> {

    val pkPrefix: String
    val skPrefix: String

    //==================================================== 기본 제공 쿼리들 ======================================================

    /** 인덱스 없이 SK 프리픽스 조회 */
    suspend fun findBySkPrefix(item: B, last: Map<String, AttributeValue>? = null): Pair<A, DynamoExpression> {
        val ddb = convertFrom(item)
        return ddb to DynamoExpressionSet.SkPrefix {
            tableName = ddb.tableName
            pk = ddb.pk
            sk = ddb.sk
            exclusiveStartKey = last
        }
    }

    /** 인덱스로 프리픽스 조회 */
    suspend fun findBySkPrefix(index: DdbBasicGsi, item: B, last: Map<String, AttributeValue>? = null): Pair<A, DynamoExpression> {
        val ddb = convertFrom(item)
        val indexValue = ddb.indexValue(index) ?: throw RuntimeException("index value not found : $index")
        log.debug { " -> [${item!!::class.name()}] ${index.name} : $indexValue" }
        return ddb to DynamoExpressionSet.SkPrefix {
            tableName = ddb.tableName
            pkName = index.pkName
            skName = index.skName
            indexName = index.indexName
            pk = indexValue.first
            sk = indexValue.second
            select = Select.AllProjectedAttributes  //기본키값만 조회 후
            exclusiveStartKey = last
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
    }


}