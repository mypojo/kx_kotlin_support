package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.*
import aws.sdk.kotlin.services.dynamodb.getItem
import aws.sdk.kotlin.services.dynamodb.model.*
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.dynamo.query.DynamoExpression
import net.kotlinx.aws.dynamo.query.DynamoResult
import net.kotlinx.aws.regist
import net.kotlinx.collection.doUntil

val AwsClient.dynamo: DynamoDbClient
    get() = getOrCreateClient { DynamoDbClient { awsConfig.build(this) }.regist(awsConfig) }

//==================================================== 기본 4종 ======================================================

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

//==================================================== scan ======================================================

/**
 * 간단 스캔
 * 1M 단위 용량으로 리턴한다.
 * 용량 이내라면 limit 제한해서 리턴
 * xxxx last 부분 다시 설계할것
 * @param data 테이블정보 및 변환용.
 *  */
suspend fun <T : DynamoData> DynamoDbClient.scan(data: T, exp: DynamoExpression? = null, last: Map<String, AttributeValue>? = null): DynamoResult<T> {
    val resp = this.scan {
        this.consistentRead = false //읽기 일관성 사용안함
        this.tableName = data.tableName
        this.exclusiveStartKey = last
        this.limit = 1000 //설정은 무제한임
        exp?.let {
            this.filterExpression = it.filterExpression()
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
suspend fun <T : DynamoData> DynamoDbClient.scanAll(data: T, exp: DynamoExpression? = null): List<T> {
    var last: Map<String, AttributeValue>? = null
    return doUntil {
        val result = scan(data, exp, last)
        last = result.lastEvaluatedKey
        result.datas to (last != null)
    }.flatten()
}