---
name: ddbItem
description: |
  일반 Kotlin data class를 DynamoDB DbItem 세트(data class, Converter, Repository)로 변환합니다.
  키워드: DynamoDB, DbItem, DbConverter, DbRepository, ddbJoin, ddbSplit, PK/SK, GSI
---

# DynamoDB DbItem 코드 생성 가이드

일반 Kotlin data class를 DynamoDB DbItem 구현체 3종 세트(data class, Converter, Repository)로 변환합니다.

## 필수 확인 사항
코드 생성 전에 사용자에게 다음을 확인합니다:
1. **패키지 경로**: 파일이 생성될 패키지 (예: `com.example.ddb.team`)
2. **PK 구성**: PK prefix와 PK에 포함될 필드 (예: `"team"` + `teamId`)
3. **SK 구성**: SK prefix와 SK에 포함될 필드 (예: `"adv"` + `mediaDiv` + `mediaAdvId`)
4. **GSI 필요 여부**: GSI01 인덱스가 필요한 경우 gsi01Pk/gsi01Sk 구성
5. **기본 모듈 경로**: 기존 DbItem이 위치한 모듈 하위 (다르면 확인)

## 생성 규칙

### 1. Data Class (`Xxx.kt`)
```kotlin
package {패키지}

import net.kotlinx.aws.dynamo.enhanced.DbItem
// 필요한 import 추가

/**
 * {클래스 설명}
 */
data class Xxx(
    //==================================================== 자연키 ======================================================
    /** 필드 설명 */
    val field1: Type,

    //==================================================== 데이터 ======================================================
    /** 필드 설명 */
    val field2: Type,
) : DbItem {
    override val pk: String get() = XxxConverter.toPk(field1)
    override val sk: String get() = XxxConverter.toSk(field2)
}
```

- PK/SK는 `XxxConverter`의 companion 메서드에 위임
- GSI가 있으면 `val gsi01Pk: String get() = XxxConverter.toGsi01Pk(...)` 추가
- 자연키(PK/SK 구성 필드)와 데이터 필드를 구분자 주석으로 분리
- `regTime`, `updateTime` 등 공통 필드는 `LocalDateTime` 타입

### 2. Converter (`XxxConverter.kt`)
```kotlin
package {패키지}

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.dynamo.ddbJoin
import net.kotlinx.aws.dynamo.ddbSplit
import net.kotlinx.aws.dynamo.enhanced.DbConverter
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.aws.dynamo.findOrThrow
import net.kotlinx.aws.dynamo.put
// GSI 사용 시: import net.kotlinx.aws.dynamo.multiIndex.DbMultiIndex

/**
 * DDB에 입력되는 메타데이터 ({클래스명})
 */
class XxxConverter(private val table: DbTable) : DbConverter<Xxx> {

    companion object {
        const val PK_PREFIX: String = "{pk_prefix}"
        const val SK_PREFIX: String = "{sk_prefix}"

        fun toPk(pkField: Type): String = arrayOf(PK_PREFIX, pkField).ddbJoin()
        fun toSk(skField1: Type? = null, skField2: Type? = null): String = arrayOf(SK_PREFIX, skField1, skField2).ddbJoin({총 요소 수})
        // GSI 사용 시:
        // fun toGsi01Pk(...): String = ...
        // fun toGsi01Sk(...): String = ...
    }

    override fun toAttribute(item: Xxx): Map<String, AttributeValue> {
        return buildMap {
            put(table.pkName, item.pk)
            put(table.skName, item.sk)
            // PK/SK에서 추출 가능한 자연키 필드는 넣지 않음
            // 나머지 데이터 필드만 put
            put(Xxx::field2.name, item.field2)
            // GSI 사용 시:
            // put(DbMultiIndex.GSI01.pkName, item.gsi01Pk)
            // put(DbMultiIndex.GSI01.skName, item.gsi01Sk)
        }
    }

    override fun fromAttributeMap(map: Map<String, AttributeValue>): Xxx {
        // PK/SK에서 자연키 필드를 ddbSplit()으로 추출
        val (_, pkField) = map[table.pkName]!!.asS().ddbSplit()
        val (_, skField1, skField2) = map[table.skName]!!.asS().ddbSplit()
        return Xxx(
            pkField = pkField,
            skField1 = skField1,
            // 나머지 필드는 map.findOrThrow(Xxx::필드명)
            field2 = map.findOrThrow(Xxx::field2),
        )
    }
}
```

**Converter 핵심 규칙**:
- `toPk()`, `toSk()` 는 companion object에 정의 (static 호출 가능)
- `toSk()`의 nullable 파라미터는 prefix만으로 조회할 때 사용 (Repository의 findInner에서 활용)
- `ddbJoin()` 파라미터: 총 요소 수(prefix 포함)를 명시 → `"prefix#val1#val2"` 형태 보장
- PK/SK에 포함된 자연키는 `toAttribute`에 별도로 넣지 않고, `fromAttributeMap`에서 `ddbSplit()`으로 추출
- `findOrThrow`: 일반 필드 역직렬화 (타입 자동 추론)
- `find`: nullable 필드 역직렬화 (값이 없을 수 있는 경우)
- `GsonData` 타입은 `findOrThrow`로 그대로 처리
- `Boolean` 타입은 `findOrThrow`로 처리
- `List<List<String>>` 같은 복잡한 타입은 JSON 문자열로 직렬화/역직렬화

### 3. Repository (`XxxRepository.kt`)
```kotlin
package {패키지}

import kotlinx.coroutines.flow.toList
import net.kotlinx.aws.dynamo.dynamo
import net.kotlinx.aws.dynamo.enhanced.DbRepository
import net.kotlinx.aws.dynamo.enhanced.DbTable
import net.kotlinx.aws.dynamo.enhanced.get
import net.kotlinx.aws.dynamo.enhancedExp.DbExpression
import net.kotlinx.aws.dynamo.enhancedExp.DbExpressionSet
import net.kotlinx.aws.dynamo.enhancedExp.queryAll
import net.kotlinx.koin.Koins.koinLazy
import net.kotlinx.reflect.name

/**
 * {클래스명} 레포지토리
 */
class XxxRepository : DbRepository<Xxx>() {

    override val dbTable by koinLazy<DbTable>(Xxx::class.name())

    /** 단건 조회 */
    suspend fun getItem({자연키 파라미터}): Xxx? =
        aws.dynamo.get(dbTable, XxxConverter.toPk(...), XxxConverter.toSk(...))

    /** 단건 삭제 */
    suspend fun deleteItem({자연키 파라미터}) {
        val pk = XxxConverter.toPk(...)
        val sk = XxxConverter.toSk(...)
        deleteItem(pk, sk)
    }

    //==================================================== 조회 ======================================================

    /** 내부 템플릿 */
    private fun findInner({PK 파라미터}, block: DbExpression.() -> Unit): DbExpressionSet.SkPrefix =
        DbExpressionSet.SkPrefix {
            table = dbTable
            pk = XxxConverter.toPk(...)
            sk = XxxConverter.toSk()  // SK prefix만으로 시작
            block(this)
        }

    /** 전체 조회 */
    suspend fun findAll({PK 파라미터}, block: DbExpression.() -> Unit = {}): List<Xxx> =
        aws.dynamo.queryAll<Xxx> { findInner({PK 파라미터}, block) }.toList()
}
```

**Repository 핵심 규칙**:
- `dbTable`은 `koinLazy<DbTable>(Xxx::class.name())`으로 주입
- `getItem` 커스텀: 자연키를 직접 받아서 `Converter.toPk/toSk`로 변환 후 조회
- `deleteItem` 커스텀: 자연키를 직접 받아서 삭제
- `findInner`: PK 고정 + SK prefix로 range query 하는 내부 템플릿
- `findAll`: findInner를 활용한 전체 조회 (Flow → List 변환)
- GSI 조회가 필요한 경우 `findInnerWithIndex` + `findAllWithIndex` 추가
- koin 등록은 하지 않음 (사용자가 별도로 DbItemModule에 등록)

## 타입별 import 가이드
- `LocalDateTime` → `java.time.LocalDateTime`
- `GsonData` → `net.kotlinx.json.gson.GsonData`
- `MediaDiv` → 프로젝트 내 실제 enum 경로 확인 후 import
- GSI 인덱스 → `net.kotlinx.aws.dynamo.multiIndex.DbMultiIndex`

## 파일 생성 위치
기본: 프로젝트 내 기존 DbItem이 위치한 모듈의 `src/main/kotlin/{패키지 경로}/` 하위
- 예: `com.example.ddb.team` → `{모듈}/src/main/kotlin/com/example/ddb/team/`
