package net.kotlinx.domain.ddb

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import net.kotlinx.aws.dynamo.*
import net.kotlinx.domain.job.Job
import net.kotlinx.json.gson.GsonData
import net.kotlinx.lazyLoad.LazyLatchProperty

/**
 * PK / SK 기반의 범용 데이터 조회도구
 *
 * 테이블을 공용으로 만들어 사용 &
 * 노스키마 & 준비된 인덱스(gsi)를 위해서 만들어짐
 *
 * GSI 는 최대 20개 지원
 *
 * 고성능 기반이 아님으로 사용에 주의할것!!
 */
class DdbBasic(override val pk: String, override val sk: String) : DynamoData {

    override val tableName: String
        get() = TABLE_NAME

    override fun toAttribute(): Map<String, AttributeValue> {
        //인덱스 조합. 참고로 가져올때는 인덱스 없어도 됨
        return mutableMapOf<String, AttributeValue>().apply {
            this += DynamoBasic.PK to AttributeValue.S(pk)
            this += DynamoBasic.SK to AttributeValue.S(sk)

            //==================================================== 최초 생성시 필수 입력값 ======================================================
            add(DdbBasic::ttl, ttl)
            add(DdbBasic::body, body)

            //==================================================== 공통 시스템 자동(필수) 입력값 ======================================================
            add(DdbBasicGsi.GSI01, gsi01)
            add(DdbBasicGsi.GSI02, gsi02)
            add(DdbBasicGsi.GSI03, gsi03)
            add(DdbBasicGsi.GSI04, gsi04)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : DynamoData> fromAttributeMap(map: Map<String, AttributeValue>): T = DdbBasic(
        map[DynamoBasic.PK]!!.asS(), map[DynamoBasic.SK]!!.asS()
    ).apply {
        //==================================================== 최초 생성시 필수 입력값 ======================================================
        body = map.findOrThrow(DdbBasic::body)
        ttl = map.find(Job::ttl)

        //==================================================== 공통 시스템 자동(필수) 입력값 ======================================================
        gsi01 = map.findPair(DdbBasicGsi.GSI01)
        gsi02 = map.findPair(DdbBasicGsi.GSI02)
        gsi03 = map.findPair(DdbBasicGsi.GSI03)
        gsi04 = map.findPair(DdbBasicGsi.GSI04)

    } as T

    /** TTL. */
    var ttl: Long? = null

    /** 내용  */
    var body: GsonData = GsonData.empty()

    /** GlobalSecondaryIndexes 01 */
    var gsi01: Pair<String, String>? = null

    /** GlobalSecondaryIndexes 02 */
    var gsi02: Pair<String, String>? = null

    /** GlobalSecondaryIndexes 03 */
    var gsi03: Pair<String, String>? = null

    /** GlobalSecondaryIndexes 04 */
    var gsi04: Pair<String, String>? = null

    /** 인덱스 값 참조 */
    fun indexValue(index: DdbBasicGsi): Pair<String, String>? = when (index) {
        DdbBasicGsi.GSI01 -> gsi01
        DdbBasicGsi.GSI02 -> gsi02
        DdbBasicGsi.GSI03 -> gsi03
        DdbBasicGsi.GSI04 -> gsi04
    }

    companion object {

        /** 테이블 이름을 여기서 지정 (한번만 지정 가능) */
        var TABLE_NAME: String by LazyLatchProperty()

    }


}

