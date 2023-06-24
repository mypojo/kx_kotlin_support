package net.kotlinx.aws.cost

import aws.sdk.kotlin.services.costexplorer.CostExplorerClient
import aws.sdk.kotlin.services.costexplorer.getCostAndUsage
import aws.sdk.kotlin.services.costexplorer.model.DateInterval
import aws.sdk.kotlin.services.costexplorer.model.Granularity
import net.kotlinx.core.time.toYmdF01
import java.time.LocalDate

/** map 의 내용을 전부 replace 한다. */
fun String.replaceAll(replacements: Map<String, String>): String {
    var result = this
    replacements.entries.forEach { result = result.replace(it.key, it.value) }
    return result
}

/**
 * 간단 조회 샘플
 * 최근 12개월(최대한도) 월단위 / 서비스별 조회
 * @return 월/서비스명/금액
 *  */
suspend fun CostExplorerClient.monthService(): List<CostExplorerLine> {
    val end = LocalDate.now()
    val start = end.minusYears(1).withDayOfMonth(1) //첫날부터 측정
    //start: LocalDate, end: LocalDate
    val resp = this.getCostAndUsage {
        this.granularity = Granularity.Monthly
        this.timePeriod = DateInterval {
            this.start = start.toYmdF01()
            this.end = end.toYmdF01()
        }
        this.metrics = listOf(CostExplorerUtil.BLENDED_COST)
        this.groupBy = listOf(CostExplorerUtil.BY_SERVICE)
    }
    return resp.resultsByTime!!.flatMap { resultByTime ->
        val startDate: String = resultByTime.timePeriod!!.start!!
        val yyyymm = startDate.substring(0, 7) //일단 모든 타임 페리오드는 월단이라고 간주하고 잘라준다.
        resultByTime.groups!!.map { group ->
            CostExplorerLine {
                this.timeSeries = yyyymm
                this.serviceName = group.keys!!.joinToString(",")
                    .replaceFirst("Amazon ", "")
                    .replaceAll(CostExplorerUtil.REPLACER)
                this.costValue = group.metrics!![CostExplorerUtil.BLENDED_COST]!!.amount!!.toDouble()
            }
        }
    }
        .filter { !CostExplorerUtil.IGNORES.contains(it.serviceName) } //저렴한 비용 무시
        .filter { it.costValue!! >= 0.1 } //람다, Firehose 등 등록만 해놓으면 0원 과금되는거 있음. 이런거 제거.
}
