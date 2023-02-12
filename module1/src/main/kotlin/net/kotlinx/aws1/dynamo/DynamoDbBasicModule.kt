package net.kotlinx.aws1.dynamo

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.getItem
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue.S
import aws.sdk.kotlin.services.dynamodb.model.Select
import aws.sdk.kotlin.services.dynamodb.paginators.items
import aws.sdk.kotlin.services.dynamodb.paginators.queryPaginated
import aws.sdk.kotlin.services.dynamodb.putItem
import kotlinx.coroutines.flow.collectIndexed
import net.kotlinx.core2.gson.GsonSet
import net.kotlinx.module1.reflect.DynamoReflectionUtil
import kotlin.reflect.KClass

/**
 * DDB용 간단 모듈
 * enhanced가 나오기 전 임시 사용
 * */
class DynamoDbBasicModule<T : DynamoDbBasic>(
    val dynamo: DynamoDbClient,
    val tableName: String,
    val clazz: KClass<T>,
    val snakeFromCamel: Boolean = true,
) {

    suspend fun putItem(data: DynamoDbBasic) {
        dynamo.putItem {
            this.tableName = this@DynamoDbBasicModule.tableName
            this.item = DynamoReflectionUtil.toAttributeMap(data, snakeFromCamel)
        }
    }

    /**
     * 리플렉션이 아닌 임시로 json 사용 -> 이때문에 숫자/문자만 사용 가능함
     *  */
    suspend fun getItem(data: DynamoDbBasic): T? {
        val item = dynamo.getItem {
            this.tableName = this@DynamoDbBasicModule.tableName
            this.consistentRead = false
            this.key = mapOf(
                DynamoDbBasic.pk to S(data.pk),
                DynamoDbBasic.sk to S(data.sk),
            )
        }.item ?: return null
        val resultMap = item.map {
            it.key to (it.value.asSOrNull() ?: it.value.asNOrNull())
        }.toMap()
        val json = GsonSet.TABLE_UTC.toJson(resultMap)
        return GsonSet.TABLE_UTC.fromJson(json, clazz.java)
    }

    /** sk 접두어로 검색 */
    suspend fun querySortBeginsWith(data: DynamoDbBasic): List<T> = query(
        mapOf(":${DynamoDbBasic.pk}" to S(data.pk), ":${DynamoDbBasic.sk}" to S(data.sk)),
        "${DynamoDbBasic.pk} = :${DynamoDbBasic.pk} AND begins_with(${DynamoDbBasic.sk}, :${DynamoDbBasic.sk})",
    )

    /** sk 접두어로 검색 */
    suspend fun querykeyEqualTo(data: DynamoDbBasic): List<T> {
        return query(
            mapOf(":${DynamoDbBasic.pk}" to S(data.pk)),
            "${DynamoDbBasic.pk} = :${DynamoDbBasic.pk}",
        )
    }

    /**
     * 상세 쿼리는 QueryConditional 참고
     * */
    suspend fun query(attValue: Map<String, S>, query: String): List<T> {
        val queryResult = dynamo.queryPaginated {
            this.tableName = this@DynamoDbBasicModule.tableName
            this.consistentRead = false
            this.expressionAttributeValues = attValue
            this.keyConditionExpression = query
            this.limit = 50
            this.select = Select.AllAttributes
        }

        //전체 리스트를 순회한다. 스트림 처리하고싶다면 별도 분리. 다른 스마트한 방법을 못찾음.
        val items = mutableListOf<T>()
        queryResult.items().collectIndexed { _, att ->
            val replacedMap = att.map { it.key to (it.value.asSOrNull() ?: it.value.asN()) }.toMap()
            val json = GsonSet.TABLE_UTC.toJson(replacedMap)
            val dto = GsonSet.TABLE_UTC.fromJson(json, clazz.java)
            items.add(dto)
        }
        return items.toList()
    }

}



