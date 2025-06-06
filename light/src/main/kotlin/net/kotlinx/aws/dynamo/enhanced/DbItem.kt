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

    /** API용 PK구현 map */
    fun toKeyMap(): Map<String, AttributeValue> = table().toKeyMap(pk, sk)

    /** 간단 PK 확인용 ex) 로깅 */
    fun toKeyString(): String = "${pk}:${sk}"

    /**
     * PK / SK를 조합해서 유니크한 문자열 생성.
     * file 이름으로도 사용 가능
     *  */
    fun toKeyPairString(): String = "${pk}-${sk}"

    /**
     * 테이블 정보.
     * 성능에 문제 없겠지?? 있으면 늦은 초기화
     * 일반적으로 koin은 리플렉션보다 빠르다고 함
     * */
    fun table(): DbTable = koin<DbTable>(this::class.name())

    /** DDB 콘솔 링크  */
    val dynamoItemLink: String
        get() {
            val table = this.table()
            return DynamoUtil.toItemLink(table.tableName, pk, sk, table.region)
        }


}
