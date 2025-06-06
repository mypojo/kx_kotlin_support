package net.kotlinx.aws.dynamo.enhanced

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.AwsConfig
import net.kotlinx.core.Kdsl


/**
 * DDB 객체를 입력 / 조회 하는데 필요한 각종 정보 모음
 * */
class DbTable {

    @Kdsl
    constructor(block: DbTable.() -> Unit = {}) {
        apply(block)
    }

    /** 테이블명  */
    lateinit var tableName: String

    /** 객체 변환기 */
    lateinit var converter: DbConverter<*>

    /** 제너릭용 함수 */
    fun <T : DbItem> conv(): DbConverter<T> = converter as DbConverter<T>

    //==================================================== 후크 ======================================================

    /** 입력전 전처리 ex) TTL */
    var beforePut: (DbItem) -> Unit = {}

    /** 영속화 할지 여부.  */
    var persist: (DbItem) -> Boolean = { true }

    //==================================================== 거의 고정 ======================================================

    /** PK 컬럼 이름.  */
    var pkName: String = PK_NAME

    /** SK 컬럼 이름.  */
    var skName: String = SK_NAME

    /** DDB가 있는 리전  */
    var region: String = AwsConfig.REGION_KR

    /** 표준 API용 키 변환 */
    fun toKeyMap(pk: String, sk: String): Map<String, AttributeValue> = mapOf(
        pkName to AttributeValue.S(pk),
        skName to AttributeValue.S(sk),
    )


    companion object {
        const val PK_NAME = "pk"
        const val SK_NAME = "sk"
    }


}