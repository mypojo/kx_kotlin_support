package net.kotlinx.aws.cost

import aws.sdk.kotlin.services.costexplorer.model.GetCostAndUsageResponse
import aws.sdk.kotlin.services.costexplorer.model.GroupDefinitionType
import net.kotlinx.string.toTextGrid

/** 간단출력 */
fun List<CostExplorerLine>.print() {
    this.map {
        arrayOf(it.projectName, it.timeSeries, it.key, it.costValue)
    }.also {
        listOf("projectName", "timeSeries", "serviceName", "costValue").toTextGrid(it).print()
    }
}

/**
 * 통계치 데이터
 * json 변환 가능하도록 기본 값만 사용
 *  */
class CostExplorerLine(block: CostExplorerLine.() -> Unit = {}) {

    /** 프로젝트명 */
    var projectName: String? = null

    /** 시계열 */
    var timeSeries: String? = null

    /** 달러화임으로 적당히 라운드 처리 */
    var costValue: Double? = null

    /**
     * 그룹바이 형태
     * @see GroupDefinitionType
     *  */
    lateinit var groupDefinitionType: String

    /**
     * group 디멘션 키값
     * ex) 서비스명 or cost tag명
     * */
    var key: String? = null

    init {
        block(this)
    }
}

fun GetCostAndUsageResponse.toLines(groupBy: GroupDefinitionType): List<CostExplorerLine> {
    return this.resultsByTime!!.flatMap { resultByTime ->
        val startDate: String = resultByTime.timePeriod!!.start
        val yyyymm = startDate.substring(0, 7) //일단 모든 타임 페리오드는 월단이라고 간주하고 잘라준다.
        resultByTime.groups!!.map { group ->
            CostExplorerLine {
                this.groupDefinitionType = groupBy.value
                this.timeSeries = yyyymm
                /** 단순 문자열로 변경해준다 */
                this.key = group.keys!!.joinToString(",") {
                    when {
                        //태그인 경우 (접두어 제거 따로 안함)
                        it.contains("$") -> it
                        //서비스 이름인 경우 짧게 수정
                        else -> it.replaceFirst("Amazon ", "").replaceAll(CostExplorerUtil.REPLACER)
                    }
                }
                this.costValue = group.metrics!![CostExplorerUtil.BLENDED_COST]!!.amount!!.toDouble()
            }
        }
    }
        .filter { !CostExplorerUtil.IGNORES.contains(it.key) } //저렴한 비용 무시
        .filter { it.costValue!! >= 0.1 } //람다, Firehose 등 등록만 해놓으면 0원 과금되는거 있음. 이런거 제거.
}