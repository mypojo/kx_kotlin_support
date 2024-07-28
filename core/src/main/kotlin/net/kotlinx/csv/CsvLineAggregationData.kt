package net.kotlinx.csv

import java.math.BigDecimal

class CsvLineAggregationData {

    /** 합계 */
    var sum: BigDecimal = BigDecimal.ZERO

    /** 디스팅트값 */
    val distinct: MutableSet<String> = mutableSetOf()

}