package net.kotlinx.aws.cost

import aws.sdk.kotlin.services.costexplorer.CostExplorerClient
import aws.sdk.kotlin.services.costexplorer.createCostCategoryDefinition
import aws.sdk.kotlin.services.costexplorer.getCostAndUsage
import aws.sdk.kotlin.services.costexplorer.model.*
import aws.sdk.kotlin.services.costexplorer.startCostAllocationTagBackfill
import net.kotlinx.time.toYmdF01
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
suspend fun CostExplorerClient.monthService(
    /** 오늘기준 1년전 01일 부터 측정 */
    start: LocalDate = LocalDate.now().minusYears(1).withDayOfMonth(1),
    /** 오늘 */
    end: LocalDate = LocalDate.now(),
): List<CostExplorerLine> {
    return this.getCostAndUsage {
        this.granularity = Granularity.Monthly
        this.timePeriod = DateInterval {
            this.start = start.toYmdF01()
            this.end = end.toYmdF01()
        }
        this.metrics = listOf(CostExplorerUtil.BLENDED_COST)
        this.groupBy = listOf(CostExplorerUtil.BY_SERVICE)
    }.toLines(GroupDefinitionType.Dimension)
}

/**
 * 간단 조회 샘플
 * 최근 12개월(최대한도) 월단위 / 태그별 조회 (미리 태그가 CostExplorer에 등록되어있어야 함)
 * @return 월/태그명/금액
 *  */
suspend fun CostExplorerClient.monthTag(
    tags: List<String>,
    /** 오늘기준 1년전 01일 부터 측정 */
    start: LocalDate = LocalDate.now().minusYears(1).withDayOfMonth(1),
    /** 오늘 */
    end: LocalDate = LocalDate.now(),
): List<CostExplorerLine> {
    return this.getCostAndUsage {
        this.granularity = Granularity.Monthly
        this.timePeriod = DateInterval {
            this.start = start.toYmdF01()
            this.end = end.toYmdF01()
        }
        this.metrics = listOf(CostExplorerUtil.BLENDED_COST)
        this.groupBy = CostExplorerUtil.groupByTags(tags)
    }.toLines(GroupDefinitionType.Tag)

}

/**
 * 비용할당태그(Cost allocation tags) 생성
 * SDK로 아직 안되는듯?
 * */
suspend fun CostExplorerClient.startCostAllocationTagBackfill() {
    this.startCostAllocationTagBackfill {

    }
    throw UnsupportedOperationException()
}



/**
 * 코스트 카테고리 정의 생성
 * 샘플 코드
 *  */
suspend fun CostExplorerClient.createCostCategoryDefinition() {
    this.createCostCategoryDefinition {
        name = "MyCostCategory"
        rules = listOf(
            CostCategoryRule {
                value = "TeamA"
                rule = Expression {
                    dimensions = DimensionValues {
                        key = Dimension.Service
                    }
                }
            },
        )
    }
}

