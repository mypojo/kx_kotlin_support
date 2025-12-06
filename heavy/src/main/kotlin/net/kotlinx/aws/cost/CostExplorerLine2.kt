package net.kotlinx.aws.cost

/**
 * CSV 업로드를 위한 간단 결과 객체
 * time(YYYY-MM), tag(프로젝트 태그 값), key(서비스명), cost(달러)
 */
data class CostExplorerLine2(
    val time: String,
    val tag: String,
    val key: String,
    val cost: Double,
)
