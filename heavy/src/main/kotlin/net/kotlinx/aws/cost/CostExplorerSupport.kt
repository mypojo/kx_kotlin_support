package net.kotlinx.aws.cost

import aws.sdk.kotlin.services.costexplorer.CostExplorerClient
import aws.sdk.kotlin.services.costexplorer.createCostCategoryDefinition
import aws.sdk.kotlin.services.costexplorer.getCostAndUsage
import aws.sdk.kotlin.services.costexplorer.model.*
import aws.sdk.kotlin.services.costexplorer.startCostAllocationTagBackfill
import net.kotlinx.aws.AwsClient
import net.kotlinx.aws.regist
import net.kotlinx.string.replaceAll
import net.kotlinx.string.toTextGrid
import net.kotlinx.time.toYmdF01
import java.time.LocalDate

val AwsClient.cost: CostExplorerClient
    get() = getOrCreateClient { CostExplorerClient { awsConfig.build(this) }.regist(awsConfig) }

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

/** 간단출력 */
fun List<CostExplorerLine>.print() {
    this.map {
        arrayOf(it.projectName, it.timeSeries, it.key, it.costValue)
    }.also {
        listOf("projectName", "timeSeries", "serviceName", "costValue").toTextGrid(it).print()
    }
}

fun GetCostAndUsageResponse.toLines(groupBy: GroupDefinitionType): List<CostExplorerLine> {
    return this.resultsByTime!!.flatMap { resultByTime ->
        val startDate: String = resultByTime.timePeriod!!.start
        val yyyymm = startDate.take(7) //일단 모든 타임 페리오드는 월단이라고 간주하고 잘라준다.
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

