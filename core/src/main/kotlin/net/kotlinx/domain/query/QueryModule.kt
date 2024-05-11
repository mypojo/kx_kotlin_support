package net.kotlinx.domain.query

/**
 * 비정형 쿼리 생성기
 * */
class QueryModule(
    private val name: String,
    private val configs: List<QueryData>,
) {

    private var configMap: Map<String, QueryData> = configs.associateBy { it.name }

    /** 조회 SQL 생성 */
    fun build(inputs: List<String>, where: () -> String): String {
        val inputDatas = inputs.map { configMap[it]!! }

        val tableEntry = inputDatas.flatMap { it.tables }.groupBy { it }.entries.maxBy { it.value.size }
        check(tableEntry.value.size == inputDatas.size) { "조회할 수 없는 컬럼 있음" }

        val dims = inputDatas.filterIsInstance<QueryDimension>()

        return """
SELECT ${inputDatas.joinToString(",") { it.format }}
FROM ${tableEntry.key}
WHERE ${where()}
GROUP BY ${dims.joinToString(",") { it.name }} 
        """.trimIndent()
    }


}
