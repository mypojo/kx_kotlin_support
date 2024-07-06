package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.*
import aws.sdk.kotlin.services.dynamodb.getItem
import aws.sdk.kotlin.services.dynamodb.model.*
import net.kotlinx.collection.doUntil

//==================================================== 트랜잭션 ======================================================
//데이터 타입은 아래 문서 참고
//https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.NamingRulesDataTypes.html#HowItWorks.DataTypes
//update 참고
//https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.UpdateExpressions.html#Expressions.UpdateExpressions.ADD

/** 해당 로우의 특정 컬럼값에 synch하게  +1 후 리턴 */
suspend fun DynamoDbClient.increaseAndGet(incTableName: String, pkName: String, pkValue: String, columnName: String): Long {
    val resp = this.updateItem {
        this.tableName = incTableName
        this.updateExpression = "set $columnName = $columnName + :val"
        this.expressionAttributeValues = mapOf(":val" to AttributeValue.N("1")) //1씩 늘림
        this.key = mapOf(pkName to AttributeValue.S(pkValue))
        this.returnValues = ReturnValue.AllNew //새 값 리턴
    }
    return resp.attributes!![columnName]!!.asN().toLong()
}

/**
 * 해당 로우의 특정 map type(M – Map) 컬럼값에 synch하게  add(오버라이드) 후 리턴하는 샘플
 * 1. 기존 map에 데이터가 많아도, 추가만 수행한다.
 * 2. 해당 컬럼에 map이 없으면 작동하지 않는다. 빈 map 이라도 반드시 존재해야 한다
 * */
suspend fun DynamoDbClient.updateItemMap(tableName: String, pk: String, sk: String, columnName: String, append: Map<String, String>) {
    this.updateItem {
        this.tableName = tableName
        this.returnValues = ReturnValue.None //필요한 경우 없음.
        this.key = mapOf(
            DynamoDbBasic.PK to AttributeValue.S(pk),
            DynamoDbBasic.SK to AttributeValue.S(sk),
        )
        this.updateExpression = "set " + append.entries.joinToString(",") { "${columnName}.${it.key} = :${it.key}" }
        this.expressionAttributeValues = append.map { ":${it.key}" to AttributeValue.S(it.value) }.toMap()
    }
}

//==================================================== 단일 ======================================================

/** 결과를 별도로 파싱하지 않음 */
suspend fun DynamoDbClient.putItem(data: DynamoData): PutItemResponse = this.putItem {
    this.tableName = data.tableName
    this.item = data.toAttribute()
}

/**
 * 간단 업데이트.  List<KProperty<*>> 사용할것
 * DDB는 batch update 같은게 아직 없는듯.
 *  */
suspend fun DynamoDbClient.updateItem(data: DynamoData, updateKeys: List<String>): UpdateItemResponse {
    return this.updateItem {
        this.tableName = data.tableName
        this.key = data.toKeyMap()
        this.updateExpression = "SET ${updateKeys.joinToString(",") { "$it = :${it}" }}"
        this.expressionAttributeValues = data.toAttribute().filterKeys { it in updateKeys }.mapKeys { ":${it.key}" }
        this.returnValues = ReturnValue.AllNew
    }
}

/** 간단 삭제 */
suspend fun DynamoDbClient.deleteItem(data: DynamoData, returnValue: ReturnValue = ReturnValue.None): DeleteItemResponse = this.deleteItem {
    this.tableName = data.tableName
    this.key = data.toKeyMap()
    this.returnValues = returnValue
}

suspend fun <T : DynamoData> DynamoDbClient.getItem(data: T): T? {
    val map: Map<String, AttributeValue> = this.getItem {
        this.tableName = data.tableName
        this.consistentRead = false
        this.key = data.toKeyMap()
    }.item ?: return null
    return data.fromAttributeMap(map)
}


/**
 * 간단 스캔
 * 1M 단위 용량으로 리턴한다.
 * 용량 이내라면 limit 제한해서 리턴
 * @param data 테이블정보 및 변환용.
 *  */
suspend fun <T : DynamoData> DynamoDbClient.scan(data: T, exp: DynamoExpress? = null, last: Map<String, AttributeValue>? = null): DynamoResult<T> {
    val resp = this.scan {
        this.consistentRead = false //읽기 일관성 사용안함
        this.tableName = data.tableName
        this.exclusiveStartKey = last
        this.limit = 1000 //설정은 무제한임
        exp?.let {
            this.filterExpression = it.expression()
            this.expressionAttributeValues = it.expressionAttributeValues()
        }
    }
    val items = resp.items!!.map { data.fromAttributeMap<T>(it) }
    return DynamoResult(items, resp.lastEvaluatedKey)
}

/**
 * 간단 스캔. 사용시 메모리 / 비용 주의!!
 * 쿼리와는 다르게 단순 doUntil 을 사용함
 * @param data 테이블정보 및 변환용.
 *  */
suspend fun <T : DynamoData> DynamoDbClient.scanAll(data: T, exp: DynamoExpress? = null): List<T> {
    var last: Map<String, AttributeValue>? = null
    return doUntil {
        val result = scan(data, exp, last)
        last = result.lastEvaluatedKey
        result.datas to (last != null)
    }.flatten()
}