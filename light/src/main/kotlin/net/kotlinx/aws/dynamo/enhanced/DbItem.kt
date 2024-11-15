package net.kotlinx.aws.dynamo.enhanced

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.dynamo.DynamoUtil
import net.kotlinx.koin.Koins.koin
import net.kotlinx.reflect.name


/** 권장하는 키값 스타일 마킹 인터페이스 */
interface DbItem {

    val pk: String
    val sk: String

    //==================================================== 기본 메소드들 ======================================================

    /** PK로 사용됨 */
    fun toKeyMap(): Map<String, AttributeValue> {
        return mapOf(
            DbTable.PK_NAME to AttributeValue.S(this.pk),
            DbTable.SK_NAME to AttributeValue.S(this.sk),
        )
    }

    /** 간단 PK 확인용 ex) 로깅 */
    fun toKeyString(): String = "${pk}:${sk}"

    /**
     * 테이블 정보.
     * 성능에 문제 없겠지?? 있으면 늦은 초기화
     * */
    fun table(): DbTable = koin<DbTable>(this::class.name())

    /** DDB 콘솔 링크  */
    val dynamoItemLink: String
        get() {
            val table = this.table()
            return DynamoUtil.toItemLink(table.tableName, pk, sk, table.region)
        }


}
