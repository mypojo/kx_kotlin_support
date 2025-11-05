package net.kotlinx.aws.dynamo

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.ReturnValue
import aws.sdk.kotlin.services.dynamodb.updateItem
import net.kotlinx.aws.dynamo.enhanced.DbTable

//==================================================== 트랜잭션 ======================================================
//데이터 타입은 아래 문서 참고
//https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.NamingRulesDataTypes.html#HowItWorks.DataTypes
//update 참고
//https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.UpdateExpressions.html#Expressions.UpdateExpressions.ADD

/** 해당 로우의 특정 컬럼값에 synch하게  +1 후 리턴 */
suspend fun DynamoDbClient.increaseAndGet(tableName: String, pk: String, sk: String, columnName: String): Long {
    val resp = this.updateItem {
        this.tableName = tableName
        this.updateExpression = "set $columnName = $columnName + :val"
        this.expressionAttributeValues = mapOf(":val" to AttributeValue.N("1")) // 1씩 늘림
        this.key = mapOf(
            DbTable.PK_NAME to AttributeValue.S(pk),
            DbTable.SK_NAME to AttributeValue.S(sk),
        )
        this.returnValues = ReturnValue.AllNew // 새 값 리턴
    }
    return resp.attributes!![columnName]!!.asN().toLong()
}

/** Map 컬럼의 특정 키 값을 증가시키고 새 값을 리턴 */
suspend fun DynamoDbClient.addMapSynch(tableName: String, pk: String, sk: String, columnName: String, mapKey: String, incrementValue: Long = 1): Long {
    val resp = this.updateItem {
        this.tableName = tableName
        this.updateExpression = "SET #mapCol.#mapKey = if_not_exists(#mapCol.#mapKey, :zero) + :val"
        this.expressionAttributeNames = mapOf(
            "#mapCol" to columnName,
            "#mapKey" to mapKey
        )
        this.expressionAttributeValues = mapOf(
            ":zero" to AttributeValue.N("0"),
            ":val" to AttributeValue.N(incrementValue.toString())
        )
        this.key = mapOf(
            DbTable.PK_NAME to AttributeValue.S(pk),
            DbTable.SK_NAME to AttributeValue.S(sk),
        )
        this.returnValues = ReturnValue.AllNew
    }
    return resp.attributes!![columnName]!!.asM()[mapKey]!!.asN().toLong()
}

/**
 * Map 컬럼의 여러 키 값을 동시에 증가시킨 후, 증가된(새) 값을 리턴
 * - updateMap 참고한 다중 파라미터 버전
 * - append: 증가시킬 대상과 증가값(Long)
 */
suspend fun DynamoDbClient.addMapSynch(tableName: String, pk: String, sk: String, columnName: String, append: Map<String, Long>): Map<String, Long> {
    if (append.isEmpty()) return emptyMap()

    // placeholder 는 키에 안전하게 v0, v1 ... 형태로 생성한다
    val valuePlaceholders = append.entries.mapIndexed { idx, (k, v) -> "v$idx" to (k to v) }.toMap()

    val setExpr = valuePlaceholders.entries.joinToString(",") { (ph, kv) ->
        val keyInMap = kv.first
        "#mapCol.$keyInMap = if_not_exists(#mapCol.$keyInMap, :zero) + :$ph"
    }

    val expressionValues: Map<String, AttributeValue> = buildMap {
        put(":zero", AttributeValue.N("0"))
        valuePlaceholders.forEach { (ph, kv) ->
            put(":$ph", AttributeValue.N(kv.second.toString()))
        }
    }

    val resp = this.updateItem {
        this.tableName = tableName
        this.returnValues = ReturnValue.AllNew
        this.key = mapOf(
            DbTable.PK_NAME to AttributeValue.S(pk),
            DbTable.SK_NAME to AttributeValue.S(sk),
        )
        this.updateExpression = "SET $setExpr"
        this.expressionAttributeNames = mapOf("#mapCol" to columnName)
        this.expressionAttributeValues = expressionValues
    }

    val updatedMap = resp.attributes!![columnName]!!.asM()
    return append.keys.associateWith { k -> updatedMap[k]!!.asN().toLong() }
}

/**
 * 해당 로우의 특정 map type(M – Map) 컬럼값에 synch하게 add(오버라이드) 수행
 * 1. 기존 map에 데이터가 많아도, 추가만 수행한다.
 * 2. 해당 컬럼에 map이 없으면 작동하지 않는다. 빈 map 이라도 반드시 존재해야 한다
 * 3. map의 키(name 등)가 예약어여도 안전하게 동작하도록 Expression Attribute Names를 사용한다.
 */
suspend fun DynamoDbClient.updateMap(tableName: String, pk: String, sk: String, columnName: String, append: Map<String, String>) {
    if (append.isEmpty()) return

    // 키/값에 대해 안전한 플레이스홀더를 생성한다 (#k0, :v0 ...)
    val items = append.entries.mapIndexed { idx, (k, v) -> Triple(idx, k, v) }

    val setExpr = items.joinToString(",") { (idx, _, _) -> "#mapCol.#k$idx = :v$idx" }

    val exprNames = buildMap {
        put("#mapCol", columnName)
        items.forEach { (idx, key, _) -> put("#k$idx", key) }
    }

    val exprValues = items.associate { (idx, _, value) -> ":v$idx" to AttributeValue.S(value) }
    this.updateItem {
        this.tableName = tableName
        this.returnValues = ReturnValue.None // 필요한 경우 없음.
        this.key = mapOf(
            DbTable.PK_NAME to AttributeValue.S(pk),
            DbTable.SK_NAME to AttributeValue.S(sk),
        )
        this.updateExpression = "set $setExpr"
        this.expressionAttributeNames = exprNames
        this.expressionAttributeValues = exprValues
    }
}