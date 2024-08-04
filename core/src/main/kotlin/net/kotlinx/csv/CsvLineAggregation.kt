package net.kotlinx.csv

import net.kotlinx.collection.MapTree
import net.kotlinx.core.Kdsl
import net.kotlinx.string.toBigDecimal2

/**
 * CSV를 읽고 특정 로우를 계산해줌
 * ex) 대용량 파일에서 특정 로우의 distinct 값 혹은 sum을 구하고 싶을때
 * 성능, 접근 이런거 무시한다!!
 * */
class CsvLineAggregation : (List<String>) -> Unit {

    @Kdsl
    constructor(block: CsvLineAggregation.() -> Unit = {}) {
        apply(block)
    }

    //==================================================== 입력 ======================================================

    /** sum 대상 로우 */
    var sumIndexs: Set<Int> = emptySet()

    /** distinct 대상 로우 */
    var distinctIndexs: Set<Int> = emptySet()

    /** 스킵 카운트. 1이면 헤더 한줄 스킵 */
    var skipCnt: Int = 0

    //==================================================== 결과 ======================================================

    /** 전체 로우 카운트 */
    var rowCnt: Int = 0

    /** 결과 */
    val results: MapTree<CsvLineAggregationData> = MapTree { CsvLineAggregationData() }

    /** 실제 처리 */
    override fun invoke(row: List<String>) {
        rowCnt++
        if (rowCnt <= skipCnt) return
        for (distinctIndex in distinctIndexs) {
            val value = row[distinctIndex]
            results["$distinctIndex"].distinct.add(value)
        }

        for (sumIndex in sumIndexs) {
            val value = row[sumIndex].toBigDecimal2()
            results["$sumIndex"].sum += value
        }

    }

}