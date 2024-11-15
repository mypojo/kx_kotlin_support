package net.kotlinx.aws.dynamo.enhancedExp

import net.kotlinx.aws.dynamo.DynamoMap
import net.kotlinx.aws.dynamo.enhanced.DbItem
import net.kotlinx.aws.dynamo.enhanced.DbTable

/**
 * 페이징용 DDB 결과 래퍼
 * 제너릭을 편의상 다 빼버렸다
 * 슈가 코드 추가하기 시작하면 끝이 없으니 그냥 쓰자..
 * */
data class DbResult(
    /** 테이블 */
    val table: DbTable,
    /** 원본 맵 */
    val maps: List<DynamoMap>,
    /** 페이징 키 */
    val lastEvaluatedKey: DynamoMap? = null,
) {

    /** 변환된 데이터 */
    val datas: List<DbItem> by lazy { maps.map { table.converter.fromAttributeMap(it) } }

    /** 제너릭 리턴 */
    fun <T : DbItem> datas(): List<T> = datas as List<T>

}