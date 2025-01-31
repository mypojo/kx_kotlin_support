package net.kotlinx.csv.flow

data class CsvFlowChunk(

    /** 0부터 시작 */
    val index: Int,

    /** 청크 안에 담긴 로우 */
    val rows: List<List<String>>,

    /** CSV 헤더 */
    val header: List<String>? = null,
)