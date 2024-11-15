package net.kotlinx.aws.dynamo.multiIndex

import net.kotlinx.aws.dynamo.enhanced.DbItem
import net.kotlinx.json.gson.GsonData

/**
 * PK / SK 기반의 범용 데이터 조회도구
 *
 * 노스키마 & 준비된 인덱스(gsi)를 위해서 만들어짐
 * GSI 는 최대 20개 지원
 *
 * 고성능 기반이 아님으로 사용에 주의할것!!
 */
class DbMultiIndexItem(override val pk: String, override val sk: String) : DbItem {

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
    fun indexValue(index: DbMultiIndex): Pair<String, String>? = when (index) {
        DbMultiIndex.GSI01 -> gsi01
        DbMultiIndex.GSI02 -> gsi02
        DbMultiIndex.GSI03 -> gsi03
        DbMultiIndex.GSI04 -> gsi04
    }

}

