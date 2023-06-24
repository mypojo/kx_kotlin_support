package net.kotlinx.aws.cost

import net.kotlinx.core.string.toTextGrid

/** 통계치 데이터 */
class CostExplorerLine(block: CostExplorerLine.() -> Unit = {}) {

    /** 프로젝트명 */
    var projectName: String? = null

    /** 시계열 */
    var timeSeries: String? = null

    /** 서비스명 */
    var serviceName: String? = null

    /** 달러화임으로 적당히 라운드 처리 */
    var costValue: Double? = null

    /** 코스트 태그 */
    var tag: String? = null

    init {
        block(this)
    }

    companion object {

        fun print(lines: List<CostExplorerLine>) {
            lines.map {
                arrayOf(it.projectName, it.timeSeries, it.serviceName, it.costValue)
            }.also {
                listOf("projectName", "timeSeries", "serviceName", "costValue").toTextGrid(it).print()
            }
        }
    }
}